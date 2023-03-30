package com.linhua.smartwatch.mine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.linhua.smartwatch.databinding.ActivitySystemSettingsBinding

class SystemSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySystemSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySystemSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }
    }
}