package com.linhua.smartwatch.tribe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.linhua.smartwatch.databinding.ActivityTribeEditBinding

class TribeEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTribeEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTribeEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}