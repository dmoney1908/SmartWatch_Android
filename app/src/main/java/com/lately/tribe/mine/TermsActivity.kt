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
        binding.wvTerms.settings.loadWithOverviewMode = true;
        binding.wvTerms.settings.useWideViewPort = true;
        binding.wvTerms.settings.javaScriptEnabled = true
        binding.wvTerms.settings.domStorageEnabled = true
        binding.wvTerms.settings.loadsImagesAutomatically = true
        binding.wvTerms.settings.builtInZoomControls = true
        binding.wvTerms.settings.displayZoomControls = true
        binding.wvTerms.settings.useWideViewPort = true
        binding.wvTerms.settings.loadWithOverviewMode = true

        binding.wvTerms.loadUrl("https://tribesmartwatch.com/Termsofuse.html")
        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }
    }
}