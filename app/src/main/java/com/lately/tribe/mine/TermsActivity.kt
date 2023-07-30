package com.lately.tribe.mine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lately.tribe.R
import com.lately.tribe.base.CommonActivity
import com.lately.tribe.databinding.ActivityHelpBinding
import com.lately.tribe.databinding.ActivityPrivacyBinding
import com.lately.tribe.databinding.ActivityTermsBinding

class TermsActivity : CommonActivity() {
    private lateinit var binding: ActivityTermsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.wvTerms.loadUrl("file:///android_asset/Terms.html");

        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }
    }
}