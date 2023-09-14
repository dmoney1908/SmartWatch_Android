package com.lately.tribe.mine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import com.lately.tribe.R
import com.lately.tribe.base.CommonActivity
import com.lately.tribe.databinding.ActivityHelpBinding
import com.lately.tribe.databinding.ActivityPrivacyBinding
import com.lately.tribe.databinding.ActivityTermsBinding

class PrivacyActivity : CommonActivity() {
    private lateinit var binding: ActivityPrivacyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.wvPrivacy.settings.loadWithOverviewMode = true;
        binding.wvPrivacy.settings.useWideViewPort = true;
        binding.wvPrivacy.settings.javaScriptEnabled = true
        binding.wvPrivacy.settings.domStorageEnabled = true
        binding.wvPrivacy.settings.loadsImagesAutomatically = true
        binding.wvPrivacy.settings.builtInZoomControls = true
        binding.wvPrivacy.settings.displayZoomControls = true
        binding.wvPrivacy.settings.useWideViewPort = true
        binding.wvPrivacy.settings.loadWithOverviewMode = true
        binding.wvPrivacy.loadUrl("https://tribesmartwatch.com/Privacypolicy.html")
        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }
    }
}