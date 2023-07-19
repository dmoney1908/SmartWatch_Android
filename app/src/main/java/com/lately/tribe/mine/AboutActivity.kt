package com.lately.tribe.mine

import android.os.Bundle
import com.lately.tribe.base.CommonActivity
import com.lately.tribe.databinding.ActivityAboutBinding

class AboutActivity : CommonActivity() {

    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }
    }
}
