package com.linhua.smartwatch.mine

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.linhua.smartwatch.base.CommonActivity
import com.linhua.smartwatch.databinding.ActivityHelpBinding


class HelpActivity : CommonActivity() {
    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }

        binding.tvMailTo.setOnClickListener {
            val emails = mutableListOf<String>()
            val email = "hello@tribeapp.com"
            composeEmail(email,"Question & Help of Tribe")
        }
    }

    fun composeEmail(address: String, subject: String?) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, address)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

}