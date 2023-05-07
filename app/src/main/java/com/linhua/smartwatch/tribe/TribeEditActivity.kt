package com.linhua.smartwatch.tribe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.linhua.smartwatch.base.CommonActivity
import com.linhua.smartwatch.databinding.ActivityTribeEditBinding

class TribeEditActivity : CommonActivity() {
    private lateinit var binding: ActivityTribeEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTribeEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }
    }
}