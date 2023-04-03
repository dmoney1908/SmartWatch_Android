package com.linhua.smartwatch.mine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blankj.utilcode.util.ColorUtils
import com.linhua.smartwatch.R
import com.linhua.smartwatch.databinding.ActivitySystemSettingsBinding
import com.linhua.smartwatch.helper.UserData

class SystemSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySystemSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySystemSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }

        if (UserData.systemSetting.unitSettings == 0) {
            binding.tvImperial.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.tvMetric.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.ivCheckUnit1.setImageResource(R.drawable.simple_check)
            binding.ivCheckUnit2.setImageResource(R.drawable.simple_uncheck)
        } else {
            binding.tvImperial.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.tvMetric.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.ivCheckUnit1.setImageResource(R.drawable.simple_uncheck)
            binding.ivCheckUnit2.setImageResource(R.drawable.simple_check)
        }

        if (UserData.systemSetting.temprUnit == 0) {
            binding.tvF.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.tvC.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.ivCheckTempr1.setImageResource(R.drawable.simple_check)
            binding.ivCheckTempr2.setImageResource(R.drawable.simple_uncheck)
        } else {
            binding.tvF.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.tvC.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.ivCheckTempr1.setImageResource(R.drawable.simple_uncheck)
            binding.ivCheckTempr2.setImageResource(R.drawable.simple_check)
        }

        binding.llImperial.setOnClickListener {
            binding.tvImperial.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.tvMetric.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.ivCheckUnit1.setImageResource(R.drawable.simple_check)
            binding.ivCheckUnit2.setImageResource(R.drawable.simple_uncheck)
            UserData.systemSetting.unitSettings = 0
            UserData.saveSystemSetting()
        }

        binding.llMetric.setOnClickListener {
            binding.tvImperial.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.tvMetric.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.ivCheckUnit1.setImageResource(R.drawable.simple_uncheck)
            binding.ivCheckUnit2.setImageResource(R.drawable.simple_check)
            UserData.systemSetting.unitSettings = 1
            UserData.saveSystemSetting()
        }

        binding.llTemprF.setOnClickListener {
            binding.tvF.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.tvC.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.ivCheckTempr1.setImageResource(R.drawable.simple_check)
            binding.ivCheckTempr2.setImageResource(R.drawable.simple_uncheck)
            UserData.systemSetting.temprUnit = 0
            UserData.saveSystemSetting()
        }

        binding.llTemprC.setOnClickListener {
            binding.tvF.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.tvC.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.ivCheckTempr1.setImageResource(R.drawable.simple_uncheck)
            binding.ivCheckTempr2.setImageResource(R.drawable.simple_check)
            UserData.systemSetting.temprUnit = 1
            UserData.saveSystemSetting()
        }

    }
}