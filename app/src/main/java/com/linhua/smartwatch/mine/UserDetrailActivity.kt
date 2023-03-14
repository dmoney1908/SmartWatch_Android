package com.linhua.smartwatch.mine

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.linhua.smartwatch.adapter.UserDetailAdapter
import com.linhua.smartwatch.bean.DeviceItem
import com.linhua.smartwatch.bean.UserItem
import com.linhua.smartwatch.databinding.ActivityUserDetailBinding
import com.linhua.smartwatch.entity.MultipleEntity
import com.linhua.smartwatch.utils.DeviceManager
import com.linhua.smartwatch.utils.OnMultiChildClickListener
import com.lxj.xpopup.XPopup
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil

class UserDetrailActivity : AppCompatActivity() {
    private val TAG: String = this.javaClass.simpleName
    private var userDetailList = mutableListOf<UserItem>()
    private lateinit var userDetailBinding: ActivityUserDetailBinding

    private val userDetailAdapter = UserDetailAdapter(mutableListOf()).apply {
        setOnItemChildClickListener(object : OnMultiChildClickListener() {
            override fun onSingleClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                when (view.id) {
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

        userDetailList.add(UserItem(MultipleEntity.TWO).apply {
            name = "Avatar"
            avatar = null
        })

        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Nickname"
        })
        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Sex"
        })
        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Birthday"
        })
        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Height"
        })
        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Weight"
        })
        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Email"
        })
        userDetailList.add(UserItem(MultipleEntity.ONE).apply {
            name = "Personal signature"
        })

        userDetailAdapter.setNewInstance(userDetailList)
        userDetailAdapter.notifyDataSetChanged()
    }

    private fun setupData() {
        userDetailList
    }

}