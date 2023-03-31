package com.linhua.smartwatch.mine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.linhua.smartwatch.databinding.ActivityPersonalInfoBinding

class PersonalInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonalInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }
    }
}