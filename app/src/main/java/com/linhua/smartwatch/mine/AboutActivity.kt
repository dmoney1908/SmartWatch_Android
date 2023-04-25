package com.linhua.smartwatch.mine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.CommonActivity
import com.linhua.smartwatch.databinding.ActivityAboutBinding
import com.linhua.smartwatch.databinding.ActivityOxygenBinding
import com.linhua.smartwatch.utils.DeviceManager
import com.lxj.xpopup.XPopup

class AboutActivity : CommonActivity() {

    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }

        binding.tvLogout.setOnClickListener {
            XPopup.Builder(this)
                .asConfirm("", "Are you sure to logout？") {

                }.show()
        }
    }
}
