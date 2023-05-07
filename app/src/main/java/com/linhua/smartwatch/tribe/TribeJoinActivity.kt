package com.linhua.smartwatch.tribe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.linhua.smartwatch.databinding.ActivityTribeEditBinding
import com.linhua.smartwatch.databinding.ActivityTribeJoinBinding

class TribeJoinActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTribeJoinBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTribeJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}