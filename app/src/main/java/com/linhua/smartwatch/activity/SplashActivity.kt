package com.linhua.smartwatch.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.linhua.smartwatch.R
import com.linhua.smartwatch.helper.UserData
import com.linhua.smartwatch.sign.SigninActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_SmartWatch)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({
//            FirebaseAuth.getInstance().signOut()
            if (FirebaseAuth.getInstance().currentUser != null) {
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