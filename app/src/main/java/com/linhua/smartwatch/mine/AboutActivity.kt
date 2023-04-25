package com.linhua.smartwatch.mine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.linhua.smartwatch.R
import com.linhua.smartwatch.activity.MainActivity
import com.linhua.smartwatch.base.CommonActivity
import com.linhua.smartwatch.databinding.ActivityAboutBinding
import com.linhua.smartwatch.databinding.ActivityOxygenBinding
import com.linhua.smartwatch.sign.SigninActivity
import com.linhua.smartwatch.utils.DeviceManager
import com.lxj.xpopup.XPopup
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.BluetoothLe

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
