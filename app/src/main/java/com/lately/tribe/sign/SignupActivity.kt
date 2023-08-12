package com.lately.tribe.sign

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
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
import com.lately.tribe.R
import com.lately.tribe.activity.MainActivity
import com.lately.tribe.base.CommonActivity
import com.lately.tribe.databinding.ActivitySignupBinding
import com.lately.tribe.helper.UserData
import com.lately.tribe.mine.PrivacyActivity
import com.lately.tribe.mine.TermsActivity
import com.lxj.xpopup.XPopup
import java.util.*

class SignupActivity : CommonActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    private val RESULT_CODE_Google = 1002

    private var bCheckedProtocl = false

    private val termText = "I accept the "
    private val term = "Terms"
    private val policy = "Privacy Policy"

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
        setupProtocol()
    }

    private fun setupProtocol() {
        val string = SpannableString("$termText$term & $policy")

        string.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(widget.context, TermsActivity::class.java)
                startActivity(intent)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = getColor(R.color.colorAccent)
                ds.isUnderlineText = false
            }
        }, termText.length, termText.length + term.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        string.setSpan(ForegroundColorSpan(getColor(R.color.primary_blue)), termText.length, termText.length + term.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        string.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(widget.context, PrivacyActivity::class.java)
                startActivity(intent)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = getColor(R.color.colorAccent)
                ds.isUnderlineText = false
            }
        }, termText.length + term.length + " & ".length, termText.length + term.length + " & ".length + policy.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)


        string.setSpan(ForegroundColorSpan(getColor(R.color.primary_blue)), termText.length + term.length + " & ".length, termText.length + term.length + " & ".length + policy.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        binding.tvProtocol.text = string
        binding.tvProtocol.movementMethod = LinkMovementMethod.getInstance()
        binding.ivCheck.setOnClickListener {
            if (bCheckedProtocl) {
                binding.ivCheck.setImageResource(R.drawable.complex_uncheck)
            } else {
                binding.ivCheck.setImageResource(R.drawable.complex_check)
            }
            bCheckedProtocl = !bCheckedProtocl
        }
    }

    private fun checkProtocol() : Boolean {
        if (!bCheckedProtocl) {
            XPopup.Builder(this)
                .asConfirm("", "Please read and agree to the Privacy Policy and User Agreement",
                    "", "OK",
                    null, null, true).show()
        }
        return bCheckedProtocl
    }

    private fun signup (email: String, password: String) {
        if (!checkProtocol()) {
            return
        }
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
        if (!checkProtocol()) {
            return
        }
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.lately.tribe.R.string.google_client_id))
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
        if (!checkProtocol()) {
            return
        }
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