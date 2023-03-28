package com.linhua.smartwatch.sign

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.linhua.smartwatch.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.window
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        binding.tvSignin.setOnClickListener{
            finish()
        }
    }

    private fun setupViews() {

    }

}