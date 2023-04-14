package com.linhua.smartwatch.mine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blankj.utilcode.util.ColorUtils
import com.linhua.smartwatch.R
import com.linhua.smartwatch.databinding.ActivitySystemSettingsBinding
import com.linhua.smartwatch.event.MessageEvent
import com.linhua.smartwatch.helper.UserData
import com.zhj.bluetooth.zhjbluetoothsdk.bean.DeviceState
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException
import org.greenrobot.eventbus.EventBus

class SystemSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySystemSettingsBinding
    private var settings = UserData.systemSetting.deepCopy()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySystemSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }

        if (settings.unitSettings == 0) {
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

        if (settings.temprUnit == 0) {
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
            settings.unitSettings = 0
        }

        binding.llMetric.setOnClickListener {
            binding.tvImperial.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.tvMetric.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.ivCheckUnit1.setImageResource(R.drawable.simple_uncheck)
            binding.ivCheckUnit2.setImageResource(R.drawable.simple_check)
            settings.unitSettings = 1
        }

        binding.llTemprF.setOnClickListener {
            binding.tvF.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.tvC.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.ivCheckTempr1.setImageResource(R.drawable.simple_check)
            binding.ivCheckTempr2.setImageResource(R.drawable.simple_uncheck)
            settings.temprUnit = 0
        }

        binding.llTemprC.setOnClickListener {
            binding.tvF.setTextColor(ColorUtils.getColor(R.color.light_gray))
            binding.tvC.setTextColor(ColorUtils.getColor(R.color.primary_black))
            binding.ivCheckTempr1.setImageResource(R.drawable.simple_uncheck)
            binding.ivCheckTempr2.setImageResource(R.drawable.simple_check)
            settings.temprUnit = 1
        }


        binding.tvSave.setOnClickListener {
            UserData.systemSetting = settings
            UserData.saveSystemSetting(null)
            syncUnit()
        }
    }

    private fun syncUnit() {
        val deviceState = DeviceState()
        deviceState.tempUnit = if (UserData.systemSetting.temprUnit == 1) 0 else 1
        deviceState.unit = if (UserData.systemSetting.unitSettings == 1) 0 else 1
        deviceState.timeFormat = 1
        val event = MessageEvent(MessageEvent.UnitChanged)
        EventBus.getDefault().post(event)
        BleSdkWrapper.setDeviceState(deviceState, object : OnLeWriteCharacteristicListener() {
            override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {

            }

            override fun onFailed(e: WriteBleException) {
            }
        })
    }
}