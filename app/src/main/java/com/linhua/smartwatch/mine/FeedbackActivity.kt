package com.linhua.smartwatch.mine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.linhua.smartwatch.R
import com.linhua.smartwatch.databinding.ActivityAboutBinding
import com.linhua.smartwatch.databinding.ActivityFeedbackBinding
import com.linhua.smartwatch.utils.DateType
import com.lxj.xpopup.XPopup
import java.util.*

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }

    }
}