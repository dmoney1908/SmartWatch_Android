package com.linhua.smartwatch.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.linhua.smartwatch.databinding.ActivityTribeBinding

class TribeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTribeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTribeBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.baseTitleBack.setOnClickListener {
//            onBackPressed()
//        }
    }

}