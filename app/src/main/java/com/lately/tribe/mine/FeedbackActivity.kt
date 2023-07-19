package com.lately.tribe.mine

import android.os.Bundle
import com.lately.tribe.base.CommonActivity
import com.lately.tribe.databinding.ActivityFeedbackBinding
import java.util.*

class FeedbackActivity : CommonActivity() {

    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }

        binding.tvSend.setOnClickListener {

        }


    }
}