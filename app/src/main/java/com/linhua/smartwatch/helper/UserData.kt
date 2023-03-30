package com.linhua.smartwatch.helper

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.linhua.smartwatch.SmartWatchApplication
import com.zhj.bluetooth.zhjbluetoothsdk.bean.DeviceState
import com.zhj.bluetooth.zhjbluetoothsdk.bean.UserBean
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil.showToast
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException


object UserData {

    var deviceUserInfo: UserBean = UserBean()
    var isDeviceEmpty = true
    var userInfo = UserInfo()
    var isLogined = false
    var systemSetting = SystemSettings()
    var deviceConfig: DeviceState? = null
    init {
        loadUserInfo()
    }

    fun loadUserInfo() {

        val userSP: SharedPreferences = SmartWatchApplication.instance.getSharedPreferences("userInfo",
            Context.MODE_PRIVATE
        )

        try {
            userInfo.name = userSP.getString("userName", "").toString()
            userInfo.signature = userSP.getString("signature", "").toString()
            userInfo.email = userSP.getString("userEmail", "").toString()
            userInfo.sex = userSP.getInt("sex", 0)
            userInfo.age = userSP.getInt("age", 0)
            userInfo.height = userSP.getInt("height", 0)
            userInfo.weight = userSP.getInt("weight", 0)
            userInfo.birthday = userSP.getString("birthday", "").toString()

        } catch (var6: ClassNotFoundException) {
            var6.printStackTrace()
        } catch (var6: IOException) {
            var6.printStackTrace()
        }
        getAvatar()
    }

    fun saveUserInfo() {
        BleSdkWrapper.setUserInfo(deviceUserInfo, object : OnLeWriteCharacteristicListener() {
            override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                ToastUtil.showToast(SmartWatchApplication.instance, "Save successfully")
                isDeviceEmpty = false
            }

            override fun onFailed(e: WriteBleException) {
                ToastUtil.showToast(SmartWatchApplication.instance, "Fail to save")
            }
        })
    }


    //从SharedPreferences获取图片
    private fun getAvatar() {
        val sharedPreferences: SharedPreferences =
            SmartWatchApplication.instance.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        //第一步:取出字符串形式的Bitmap
        val imageString = sharedPreferences.getString("avatar", "")
        //第二步:利用Base64将字符串转换为ByteArrayInputStream
        val byteArray = Base64.decode(imageString, Base64.DEFAULT)
        if (byteArray.isEmpty()) {
            return
        } else {
            val byteArrayInputStream = ByteArrayInputStream(byteArray)
            //第三步:利用ByteArrayInputStream生成Bitmap
            val bitmap = BitmapFactory.decodeStream(byteArrayInputStream)
//            userAvatar = bitmap
        }
    }

    //保存图片到SharedPreferences
    private fun saveAvatar(bitmap: Bitmap) {
        // Bitmap bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        //第一步:将Bitmap压缩至字节数组输出流ByteArrayOutputStream
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
        //第二步:利用Base64将字节数组输出流中的数据转换成字符串String
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        val imageString: String = Base64.encodeToString(byteArray, Base64.DEFAULT)
        //第三步:将String保持至SharedPreferences
        val sharedPreferences: SharedPreferences =
            SmartWatchApplication.instance.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("avatar", imageString)
        editor.commit()
    }
}