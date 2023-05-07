package com.linhua.smartwatch.tribe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.linhua.smartwatch.databinding.ActivityTribeCreateBinding
import com.linhua.smartwatch.databinding.ActivityTribeEditBinding

class TribeCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTribeCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTribeCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}