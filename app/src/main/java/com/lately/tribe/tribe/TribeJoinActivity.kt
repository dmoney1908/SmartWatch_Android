package com.lately.tribe.tribe

import android.os.Bundle
import com.lately.tribe.R
import com.lately.tribe.base.CommonActivity
import com.lately.tribe.databinding.ActivityTribeJoinBinding
import com.lately.tribe.helper.TribeInfo
import com.lately.tribe.helper.TribeMember
import com.lately.tribe.helper.UserData

class TribeJoinActivity : CommonActivity() {
    private lateinit var binding: ActivityTribeJoinBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTribeJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }

        binding.tvContinue.setOnClickListener {
            val code: String = binding.etName.text.toString().trim { it <= ' ' }
            if (code.length != 10) {
                showToast(resources.getString(R.string.enter_tribe_code))
                return@setOnClickListener
            }
            UserData.fetchTribeDetail(code, completeBlock = {
                if (it) {
                    joinTribe(code)
                } else {
                    showToast(resources.getString(R.string.enter_tribe_code))
                }
            })
        }
    }

    private fun joinTribe(code: String) {
        var tribeInfo = TribeInfo()
        tribeInfo.code = code
        tribeInfo.role = 0
        tribeInfo.name = UserData.tribe.tribeDetail!!.name
        tribeInfo.avatar = UserData.tribe.tribeDetail!!.avatar
        UserData.tribe.tribeInfo = tribeInfo
        UserData.updateTribeInfo(null)
        val member = TribeMember(UserData.userInfo.name,
            UserData.userInfo.email,
            UserData.userInfo.avatar,
            UserData.healthData.steps,
            UserData.healthData.sleepTime,
            0,
            UserData.healthData.date)
        UserData.tribe.tribeDetail?.addMember(member)
        UserData.updateTribeDetail(null)
        finish()
    }
}