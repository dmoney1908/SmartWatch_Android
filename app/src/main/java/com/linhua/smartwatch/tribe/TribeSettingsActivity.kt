package com.linhua.smartwatch.tribe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.CommonActivity
import com.linhua.smartwatch.databinding.ActivityTribeSettingsBinding
import com.linhua.smartwatch.helper.UserData
import com.linhua.smartwatch.sign.SigninActivity
import com.linhua.smartwatch.utils.DeviceManager
import com.lxj.xpopup.XPopup
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.BluetoothLe

class TribeSettingsActivity : CommonActivity() {
    private lateinit var binding: ActivityTribeSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTribeSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }
        Glide.with(this).load(UserData.tribe.tribeInfo!!.avatar).placeholder(R.drawable.avatar_user).centerCrop()
            .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
            .into(binding.ivTribeAvatar)

        if (UserData.tribe.tribeDetail!!.members.size < 2) {
            binding.tvMemberNum.text = String.format("%d member", UserData.tribe.tribeDetail!!.members.size)
        } else {
            binding.tvMemberNum.text = String.format("%d members", UserData.tribe.tribeDetail!!.members.size)
        }

        binding.tvTribeName.text = UserData.tribe.tribeInfo!!.name

        val member = UserData.tribe.tribeDetail!!.members.firstOrNull {
            it.role == 1
        }

        if (member != null) {
            Glide.with(this).load(member.avatar).placeholder(R.drawable.avatar_user).centerCrop()
                .apply(RequestOptions.bitmapTransform(RoundedCorners(12)))
                .into(binding.ivCreatorAvatar)
            binding.tvCreatorName.text = member.name
            binding.tvCreatorEmail.text = member.email
        }
        binding.llLeaveTribe.setOnClickListener{
            XPopup.Builder(this)
                .asConfirm("", "Are you sure to leave this tribe？") {
                    UserData.leaveTribeDetail(UserData.tribe.tribeInfo!!.code, completeBlock = {})
                    UserData.deleteTribeInfo {  }
                    UserData.tribe.tribeDetail = null
                    UserData.tribe.tribeInfo = null
                    finish()
                }.show()
        }
    }
}
