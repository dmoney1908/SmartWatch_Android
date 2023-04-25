package com.linhua.smartwatch.fragment

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ColorUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseFragment
import com.linhua.smartwatch.helper.SystemSettings
import com.linhua.smartwatch.helper.UserData
import com.linhua.smartwatch.mine.*
import com.linhua.smartwatch.sign.SigninActivity
import com.linhua.smartwatch.utils.DeviceManager
import com.lxj.xpopup.XPopup
import com.yuyh.library.imgsel.ISNav
import com.yuyh.library.imgsel.config.ISCameraConfig
import com.yuyh.library.imgsel.config.ISListConfig
import com.zhj.bluetooth.zhjbluetoothsdk.bean.UserBean
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.BluetoothLe
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException


class PersonalFragment: BaseFragment(){
    var hostView: View? = null

    private val REQUEST_LIST_CODE = 1000
    private val REQUEST_CAMERA_CODE = 1001

    override fun initView(): View? {
        hostView = View.inflate(activity, R.layout.fragment_mine,null)
        hostView!!.findViewById<RelativeLayout>(R.id.rl_info).setOnClickListener {
            val intent = Intent(this.context, PersonalInfoActivity::class.java)
            startActivity(intent)
        }
        hostView!!.findViewById<RelativeLayout>(R.id.rl_person_info).setOnClickListener {
            val intent = Intent(this.context, PersonalInfoActivity::class.java)
            startActivity(intent)
        }
        hostView!!.findViewById<RelativeLayout>(R.id.rl_settings).setOnClickListener {
            val intent = Intent(this.context, SystemSettingsActivity::class.java)
            startActivity(intent)
        }
        hostView!!.findViewById<RelativeLayout>(R.id.rl_help).setOnClickListener {
            val intent = Intent(this.context, HelpActivity::class.java)
            startActivity(intent)
        }
        hostView!!.findViewById<RelativeLayout>(R.id.rl_about).setOnClickListener {
            val intent = Intent(this.context, AboutActivity::class.java)
            startActivity(intent)
        }

        ISNav.getInstance().init { context, path, imageView ->
            Glide.with(context).load(path).into(imageView)
        }
        hostView!!.findViewById<ImageView>(R.id.iv_photo).setOnClickListener {
            XPopup.Builder(this.context).atView(hostView!!.findViewById<ImageView>(R.id.iv_photo)).asAttachList(
                arrayOf("Select Photo", "Take Photo"), null
            ) { index, _ ->
                when (index) {
                    0 -> Single(null)
                    1 -> Camera(null)
                }
            }.show()
        }

        hostView!!.findViewById<TextView>(R.id.tv_logout).setOnClickListener {
            XPopup.Builder(this.context)
                .asConfirm("", "Are you sure to logout？") {
                    FirebaseAuth.getInstance().signOut()
                    val device = DeviceManager.getConnectedDevice()
                    if (device != null) {
                        val mBluetoothLe = BluetoothLe.getDefault()
                        mBluetoothLe.disconnect()
                        DeviceManager.setConnectedDevice(null)
                    }
                    startActivity(Intent(this.context, SigninActivity::class.java))
                    this.requireActivity().finish()
                }.show()
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
                if ( handlerBleDataResult.data is UserBean) {
                    UserData.deviceUserInfo = handlerBleDataResult.data as UserBean
                }
                //0x00:男 0x01:女 0x02:其他
            }

            override fun onFailed(e: WriteBleException) {
                showUserInfo()
            }
        })
    }

    private fun showUserInfo() {
        if (UserData.userInfo.name.isNotEmpty()) {
            hostView!!.findViewById<TextView>(R.id.tv_name).text = UserData.userInfo.name
        } else {
            hostView!!.findViewById<TextView>(R.id.tv_name).text = "Tribe"
        }
        if (UserData.userInfo.email.isNotEmpty()) {
            hostView!!.findViewById<TextView>(R.id.tv_email_addr).text = UserData.userInfo.email
        }
        if (UserData.userInfo.signature.isNotEmpty()) {
            hostView!!.findViewById<TextView>(R.id.tv_signature).text = UserData.userInfo.signature
        }
        if (UserData.userInfo.avatar.isNotEmpty()) {
            Glide.with(this).load(UserData.userInfo.avatar).centerCrop()
                .apply(RequestOptions.bitmapTransform(RoundedCorners(50))).into(hostView!!.findViewById<ImageView>(R.id.iv_avatar))
        }
    }

    private fun Single(view: View?) {
        val config = ISListConfig.Builder() // 是否多选
            .multiSelect(false)
            .btnText("Confirm") // 确定按钮背景色
            //.btnBgColor(Color.parseColor(""))
            // 确定按钮文字颜色
            .btnTextColor(Color.WHITE) // 使用沉浸式状态栏
            .statusBarColor(Color.parseColor("#FFFFFF")) // 设置状态栏字体风格黑色
            .isDarkStatusStyle(false) // 返回图标ResId
            .backResId(R.drawable.icon_navigation_back)
            .title("Images")
            .titleColor(ColorUtils.getColor(R.color.primary_black))
            .titleBgColor(Color.parseColor("#FFFFFF"))
            .allImagesText("All Images")
            .needCrop(true)
            .cropSize(1, 1, 200, 200) // 第一个是否显示相机
            .needCamera(true) // 最大选择图片数量
            .maxNum(1)
            .build()
        ISNav.getInstance().toListActivity(this, config, REQUEST_LIST_CODE)
    }

    private fun Camera(view: View?) {
        val config = ISCameraConfig.Builder()
            .needCrop(true)
            .cropSize(1, 1, 200, 200)
            .build()
        ISNav.getInstance().toCameraActivity(this, config, REQUEST_CAMERA_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LIST_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val pathList: List<String>? = data.getStringArrayListExtra("result")

            ///storage/emulated/0/Android/media/com.linhua.smartwatch/1680442090933.jpg

            if (pathList == null || pathList.isEmpty())return
            val path = pathList[0]
            UserData.uploadImageToFirebase(path) { complete, result ->
                if (complete && result != null) {
                    UserData.userInfo.avatar = result
                    UserData.saveUserInfo(null)
                }
            }
            val filePath = "file://$path"
            Glide.with(this).load(filePath).transform(CenterInside(), RoundedCorners(50)).into(hostView!!.findViewById<ImageView>(R.id.iv_avatar))
        } else if (requestCode == REQUEST_CAMERA_CODE && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            var path = data.getStringExtra("result")

            if (path != null) {
                UserData.uploadImageToFirebase(path) { complete, result ->
                    if (complete && result != null) {
                        UserData.userInfo.avatar = result
                        UserData.saveUserInfo(null)
                    }
                }
                val filePath = "file://$path"
                Glide.with(this).load(filePath).into(hostView!!.findViewById<ImageView>(R.id.iv_avatar))
            }
        }
    }
}