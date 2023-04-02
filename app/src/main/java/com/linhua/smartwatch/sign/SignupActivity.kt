package com.linhua.smartwatch.sign

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.linhua.smartwatch.R
import com.linhua.smartwatch.activity.MainActivity
import com.linhua.smartwatch.base.CommonActivity
import com.linhua.smartwatch.databinding.ActivitySignupBinding
import com.linhua.smartwatch.helper.UserData
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil.showToast
import java.util.*

class SignupActivity : CommonActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    private val RESULT_CODE_Google = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        firebaseAuth = FirebaseAuth.getInstance()
        this.window
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        binding.tvSignin.setOnClickListener{
            finish()
        }

        binding.tvSignup.setOnClickListener {
            val name: String = binding.etName.text.toString().trim { it <= ' ' }
            if (name.isEmpty()) {
                showToast(resources.getString(R.string.email_invalid))
                return@setOnClickListener
            }
            val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
            if (email.isEmpty()) {
                showToast(resources.getString(R.string.email_invalid))
                return@setOnClickListener
            }
            val pwd: String = binding.etPwd.text.toString().trim { it <= ' ' }
            if (pwd.isEmpty()) {
                showToast(resources.getString(R.string.pwd_invalid))
                return@setOnClickListener
            }
            UserData.userInfo.name = name
            signup(email, pwd)
        }
        binding.rlGoogle.setOnClickListener {
            googleLogin()
        }

        binding.rlFacebook.setOnClickListener {
            facebookLogin()
        }

        binding.etPwd.transformationMethod = PasswordTransformationMethod.getInstance()

        binding.ivSecure.setOnClickListener{
            if (binding.etPwd.transformationMethod == PasswordTransformationMethod.getInstance()) {
                binding.etPwd.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                binding.etPwd.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
    }

    private fun signup (email: String, password: String) {

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email
                UserData.isLogined = true
                if (email != null) {
                    UserData.userInfo.email = email
                }

                showToast("Account have been created with email $email")
                val db = Firebase.firestore
                val profile = hashMapOf(
                    "name" to UserData.userInfo.name,
                    "email" to email
                )
                db.collection("profile").document(FirebaseAuth.getInstance().currentUser!!.uid).set(
                    profile).addOnSuccessListener {
                }
                startMainActivity()
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Log.w(TAG, "signInWithEmail:failure, " + e.message)
                showToast( "Sign Up Failded")
            }
    }

    private fun googleLogin() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.linhua.smartwatch.R.string.google_client_id))
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        //启动登录，在onActivityResult方法回调
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RESULT_CODE_Google)
    }

    private var fbCallbackManager: CallbackManager? = null

    private fun facebookLogin() {
        fbCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"))
        LoginManager.getInstance()
            .registerCallback(fbCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    //facebook授权成功，去firebase验证
                    if (loginResult != null) {
                        val accessToken = loginResult.accessToken
                        if (accessToken != null) {
                            val token = accessToken.token
                            firebaseAuthWithFacebook(token)
                        }
                    }
                }

                override fun onCancel() {
                    //取消授权
                }

                override fun onError(error: FacebookException) {
                    //授权失败
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (fbCallbackManager != null) {
            fbCallbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
        when (requestCode) {
            RESULT_CODE_Google -> {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                    if (account.idToken != null) {
                        //firebase验证google登录
                        firebaseAuthWithGoogle(account.idToken!!)
                    }
                } catch (e: ApiException) {
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        when {
            idToken != null -> {
                // Got an ID token from Google. Use it to authenticate
                // with Firebase.
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success")
                            val user = firebaseAuth.currentUser
                            UserData.isLogined = true
                            if (user != null) {
                                UserData.userInfo.email = user.email.toString()
                            }
                            startMainActivity()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.exception)
                            showToast("Authentication failed.")
                        }
                    }
            }
            else -> {
                // Shouldn't happen.
                Log.d(TAG, "No ID token!")
            }
        }
    }

    private fun firebaseAuthWithFacebook(token: String) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                    UserData.isLogined = true
                    if (user != null) {
                        UserData.userInfo.email = user.email.toString()
                    }

                    startMainActivity()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    showToast("Authentication failed.")
                }
            }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        SigninActivity.closeSelf()
    }
}