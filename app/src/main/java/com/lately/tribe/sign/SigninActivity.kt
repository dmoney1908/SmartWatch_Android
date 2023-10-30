package com.lately.tribe.sign

import android.app.Activity
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
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ColorUtils
import com.facebook.CallbackManager
import com.facebook.CallbackManager.Factory.create
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
import com.lately.tribe.R
import com.lately.tribe.activity.MainActivity
import com.lately.tribe.base.CommonActivity
import com.lately.tribe.databinding.ActivitySigninBinding
import com.lately.tribe.helper.UserData
import com.lately.tribe.mine.PrivacyActivity
import com.lately.tribe.mine.TermsActivity
import com.lately.tribe.utils.DeviceManager
import com.lately.tribe.utils.IntentUtil
import com.lxj.xpopup.XPopup
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.BluetoothLe
import java.lang.ref.WeakReference
import java.util.*


class SigninActivity : CommonActivity() {
    private lateinit var binding: ActivitySigninBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private val RESULT_CODE_SIGN_UP = 1000
    private val RESULT_CODE_Google = 1001

    private var bCheckedProtocl = false

    private val termText = "I have read and agreed to the "
    private val term = "User Agreement"
    private val policy = "Privacy Policy"

    companion object {
        var mActivity: WeakReference<Activity>? = null

        fun closeSelf() {
            if (mActivity != null && mActivity!!.get() != null) {
                val context = mActivity!!.get()
                context!!.finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mActivity?.clear()
        mActivity = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        progressDialog = ProgressDialog(this)
//        progressDialog.setTitle("Please Wait")
//        progressDialog.setMessage("Creating account")
        progressDialog.setCanceledOnTouchOutside(false)
        firebaseAuth = FirebaseAuth.getInstance()

        setContentView(binding.root)
        this.window
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        binding.tvSignup.setOnClickListener {
            SigninActivity.mActivity = WeakReference<Activity>(this)
            IntentUtil.goToActivityForResult(
                this,
                SignupActivity::class.java,
                RESULT_CODE_SIGN_UP
            )
        }

        binding.tvLogin.setOnClickListener {
            val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
            if (email.isEmpty()) {
                showToast(resources.getString(com.lately.tribe.R.string.email_invalid))
                return@setOnClickListener
            }
            val pwd: String = binding.etPwd.text.toString().trim { it <= ' ' }
            if (pwd.isEmpty()) {
                showToast(resources.getString(com.lately.tribe.R.string.pwd_invalid))
                return@setOnClickListener
            }

            login(email, pwd)
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

        binding.tvForget.setOnClickListener {
            val email: String = binding.etEmail.text.toString().trim { it <= ' ' }
            if (email.isEmpty()) {
                showToast(resources.getString(com.lately.tribe.R.string.email_invalid))
                return@setOnClickListener
            }

            XPopup.Builder(this)
                .asConfirm("", "Are you sure to reset your password?") {
                    showLoading()
                    val fAuth = FirebaseAuth.getInstance()
                    fAuth.sendPasswordResetEmail(email).addOnCompleteListener { listener ->
                        hideLoading()
                        if (listener.isSuccessful) {
                            // Do something when successful
                            showToast(resources.getString(R.string.check_your_email))
                        } else {
                            showToast(resources.getString(R.string.email_not_exist))
                        }
                    }.addOnFailureListener {
                        hideLoading()
                        showToast(resources.getString(R.string.reset_failed))
                    }
                }.show()

        }

//        setupProtocol()
    }

//    private fun setupProtocol() {
//        val string = SpannableString("$termText$policy and $term")
//
//        string.setSpan(object : ClickableSpan() {
//            override fun onClick(widget: View) {
//                val intent = Intent(widget.context, PrivacyActivity::class.java)
//                startActivity(intent)
//            }
//            override fun updateDrawState(ds: TextPaint) {
//                super.updateDrawState(ds)
//                ds.color = getColor(R.color.colorAccent)
//                ds.isUnderlineText = false
//            }
//        }, termText.length, termText.length + policy.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
//        string.setSpan(ForegroundColorSpan(getColor(R.color.primary_blue)), termText.length, termText.length + policy.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
//
//        string.setSpan(object : ClickableSpan() {
//            override fun onClick(widget: View) {
//                val intent = Intent(widget.context, TermsActivity::class.java)
//                startActivity(intent)
//            }
//            override fun updateDrawState(ds: TextPaint) {
//                super.updateDrawState(ds)
//                ds.color = getColor(R.color.colorAccent)
//                ds.isUnderlineText = false
//            }
//        }, termText.length + policy.length + " and ".length, termText.length + policy.length + " and ".length + term.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
//
//
//        string.setSpan(ForegroundColorSpan(getColor(R.color.primary_blue)), termText.length + policy.length + " and ".length, termText.length + policy.length + " and ".length + term.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
//
//        binding.tvProtocol.text = string
//        binding.tvProtocol.movementMethod = LinkMovementMethod.getInstance()
//        binding.ivCheck.setOnClickListener {
//            if (bCheckedProtocl) {
//                binding.ivCheck.setImageResource(R.drawable.complex_uncheck)
//            } else {
//                binding.ivCheck.setImageResource(R.drawable.complex_check)
//            }
//            bCheckedProtocl = !bCheckedProtocl
//        }
//    }

    private fun checkProtocol() : Boolean {
        return true
//        if (!bCheckedProtocl) {
//            XPopup.Builder(this)
//                .asConfirm("", "Please read and agree to the Privacy Policy and User Agreement",
//                    "", "OK",
//                    null, null, true).show()
//        }
//        return bCheckedProtocl
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
        fbCallbackManager = create()
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

    private fun login (email: String, password: String) {
        if (!checkProtocol()) {
            return
        }
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    showToast("Login successfully")
                    val user = firebaseAuth.currentUser
                    UserData.isLogined = true
                    if (user != null) {
                        UserData.userInfo.email = user.email.toString()
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    showToast("Authentication failed.")
                }
            }.addOnFailureListener {
                Log.w(TAG, "signInWithEmail:failure", it)
                showToast("Authentication failed.")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (fbCallbackManager != null) {
            fbCallbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
        when (requestCode) {
            RESULT_CODE_SIGN_UP-> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    finish()
                }
            }
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
                    // Google Sign In failed, update UI appropriately
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
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
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
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    showToast("Authentication failed.")
                }
            }
    }

}
