package com.linhua.smartwatch.fragment

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseFragment
import com.linhua.smartwatch.helper.UserData
import com.linhua.smartwatch.mine.UserDetailActivity
import com.linhua.smartwatch.utils.DeviceManager
import com.zhj.bluetooth.zhjbluetoothsdk.bean.UserBean
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException

class PersonalFragment: BaseFragment(){
    var hostView: View? = null

    override fun initView(): View? {
        hostView = View.inflate(activity, R.layout.fragment_mine,null)
        hostView!!.findViewById<RelativeLayout>(R.id.rl_info).setOnClickListener {
            val intent = Intent(this.context, UserDetailActivity::class.java)
            startActivity(intent)
        }
        hostView!!.findViewById<RelativeLayout>(R.id.rl_person_info).setOnClickListener {
            val intent = Intent(this.context, UserDetailActivity::class.java)
            startActivity(intent)
        }
        return hostView
    }

    override fun initData() {

    }
    override fun onListener() {

    }

    override fun onResume() {
        super.onResume()
        getUserInfo()
    }

    private fun getUserInfo() {
        if (DeviceManager.getConnectedDevice() == null) {
            showUserInfo()
            return
        }
        BleSdkWrapper.getUserInfo(object : OnLeWriteCharacteristicListener() {
            override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                UserData.deviceUserInfo = handlerBleDataResult.data as UserBean
                //0x00:男 0x01:女 0x02:其他
            }

            override fun onFailed(e: WriteBleException) {
                showUserInfo()
            }
        })
    }

    private fun showUserInfo() {
        hostView!!.findViewById<TextView>(R.id.tv_name).text = UserData.userInfo.name
        if (UserData.userInfo.email.isNotEmpty()) {
            hostView!!.findViewById<TextView>(R.id.tv_email_addr).text = UserData.userInfo.email
        }
        if (UserData.userInfo.signature.isNotEmpty()) {
            hostView!!.findViewById<TextView>(R.id.tv_signature).text = UserData.userInfo.signature
        }
        if (UserData.userInfo.avatar != null) {
//            hostView!!.findViewById<ImageView>(R.id.iv_avatar).setImageBitmap(UserData.userAvatar)
        }
    }
}