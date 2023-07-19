package com.lately.tribe.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.lately.tribe.R
import com.lately.tribe.base.CommonActivity
import com.lately.tribe.helper.UserData
import com.lately.tribe.sign.SigninActivity

class SplashActivity : CommonActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_SmartWatch)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({
//            FirebaseAuth.getInstance().signOut()
            if (FirebaseAuth.getInstance().currentUser != null) {
                UserData.userInfo.email = FirebaseAuth.getInstance().currentUser!!.email.toString()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, SigninActivity::class.java)
                startActivity(intent)
            }

            finish()
        }, 1000)
    }
}