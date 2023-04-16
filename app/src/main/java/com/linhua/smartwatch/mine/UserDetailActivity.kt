package com.linhua.smartwatch.mine

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.linhua.smartwatch.R
import com.linhua.smartwatch.adapter.UserDetailAdapter
import com.linhua.smartwatch.base.CommonActivity
import com.linhua.smartwatch.bean.UserItem
import com.linhua.smartwatch.databinding.ActivityUserDetailBinding
import com.linhua.smartwatch.entity.MultipleEntity
import com.linhua.smartwatch.helper.UserData
import com.linhua.smartwatch.utils.DeviceManager
import com.linhua.smartwatch.utils.OnMultiClickListener
import com.lxj.xpopup.XPopup


class UserDetailActivity : CommonActivity() {
    private var userDetailList = mutableListOf<UserItem>()
    private lateinit var userDetailBinding: ActivityUserDetailBinding

    private val userDetailAdapter = UserDetailAdapter(mutableListOf()).apply {
        setOnItemClickListener(object : OnMultiClickListener() {
            override fun onSingleClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                when (position) {
                    0 -> {
                        XPopup.Builder(this@UserDetailActivity)
                            .asConfirm("", "Are you sure to delete this watch？") {

                            }.show()
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userDetailBinding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(userDetailBinding.root)
        userDetailBinding.rvUserDetail.apply {
            adapter = userDetailAdapter
            layoutManager = LinearLayoutManager(this.context)
        }

        userDetailBinding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }
        userDetailBinding.rvUserDetail.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        setupData()
    }

    private fun setupData() {
        userDetailList.add(UserItem(MultipleEntity.TWO).apply {
            name = "Avatar"
//            avatar = UserData.userInfo.avatar
        })

        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Nickname"
            detail = UserData.userInfo.name
        })
        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Sex"
            var sex = 0
            sex = if (!UserData.isDeviceEmpty) {
                UserData.deviceUserInfo.gender
            } else {
                UserData.userInfo.sex
            }
            detail = sex.toString()
        })
        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Birthday"
            detail = if (!UserData.isDeviceEmpty) ({
                if (UserData.deviceUserInfo.year == 0 && UserData.deviceUserInfo.month == 0 && UserData.deviceUserInfo.day == 0){
                    String.format("%d/%02d/%02d", UserData.deviceUserInfo.year, UserData.deviceUserInfo.month, UserData.deviceUserInfo.day)
                } else {
                    ""
                }
            }).toString() else {
                UserData.userInfo.birthday
            }
        })
        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Height"
            var height = 0
            height = if (!UserData.isDeviceEmpty) {
                UserData.deviceUserInfo.height
            } else {
                UserData.userInfo.height
            }
            if (height == 0) {
                height = 170
            }
            detail = height.toString() + "cm"
        })
        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Weight"
            var weight = 0
            weight = if (!UserData.isDeviceEmpty) {
                UserData.deviceUserInfo.weight
            } else {
                UserData.userInfo.weight
            }
            if (weight == 0) {
                weight = 600
            }
            detail = String.format("%.1f", weight / 10.0) + "kg"

        })
        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Email"
            detail = UserData.userInfo.email
        })
        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Personal signature"
            detail = UserData.userInfo.signature
        })

        userDetailAdapter.setNewInstance(userDetailList)
        userDetailAdapter.notifyDataSetChanged()
    }

}