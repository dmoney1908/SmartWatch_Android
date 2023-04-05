package com.linhua.smartwatch.helper

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.linhua.smartwatch.SmartWatchApplication
import com.zhj.bluetooth.zhjbluetoothsdk.bean.DeviceState
import com.zhj.bluetooth.zhjbluetoothsdk.bean.UserBean
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil.showToast
import java.io.*
import java.util.*


object UserData {

    var deviceUserInfo: UserBean = UserBean()
    var isDeviceEmpty = true
    var userInfo = UserInfo()
    var isLogined = false
    var systemSetting = SystemSettings()
    var deviceConfig: DeviceState? = null
    var lastMac = ""
    init {
        loadUserInfo()
        loadSystemSetting()
    }

    fun loadSystemSetting() {
        val userSP: SharedPreferences = SmartWatchApplication.instance.getSharedPreferences("settings",
            Context.MODE_PRIVATE
        )
        val db = Firebase.firestore
        val docRef = db.collection("settings").document(FirebaseAuth.getInstance().currentUser!!.uid)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            val settings = documentSnapshot.toObject<SystemSettings>()
            if (settings != null) {
                systemSetting = settings
            }
        }
    }

    fun saveMac(mac: String?) {
        lastMac = mac ?: ""
        val userSP: SharedPreferences = SmartWatchApplication.instance.getSharedPreferences("settings",
            Context.MODE_PRIVATE
        )

        try {
            val editor = userSP.edit()
            editor.putString("lastMac", lastMac)
            editor.apply()
        } catch (var6: IOException) {
            var6.printStackTrace()
        }
    }

    fun saveSystemSetting(completeBlock : ((complete: Boolean) -> Unit)?) {
        val db = Firebase.firestore
        val settings = hashMapOf(
            "unitSettings" to systemSetting.unitSettings,
            "temprUnit" to systemSetting.temprUnit,
            "dataShare" to systemSetting.dataShare
        )

        db.collection("settings").document(FirebaseAuth.getInstance().currentUser!!.uid).set(
            settings).addOnSuccessListener {
            if (completeBlock != null) {
                completeBlock(true)
            }

        }.addOnFailureListener {
            if (completeBlock != null) {
                completeBlock(false)
            }
        }
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

    fun saveUserInfo(completeBlock : ((complete: Boolean) -> Unit)?) {
        val db = Firebase.firestore
        val profile = hashMapOf(
            "name" to userInfo.name,
            "avatar" to userInfo.avatar,
            "signature" to userInfo.signature,
            "email" to userInfo.email,
            "sex" to userInfo.sex,
            "height" to userInfo.height,
            "weight" to userInfo.weight,
            "birthday" to userInfo.birthday,
        )

        db.collection("profile").document(FirebaseAuth.getInstance().currentUser!!.uid).set(
            profile).addOnSuccessListener {
            if (completeBlock != null) {
                completeBlock(true)
            }

        }.addOnFailureListener {
            if (completeBlock != null) {
                completeBlock(false)
            }
        }
    }

    fun saveUserInfoToDevice() {
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

    fun uploadImageToFirebase(path: String, completeBlock : ((complete: Boolean, result: String?) -> Unit)) {
        if (path != null) {
            val storage = Firebase.storage
            val storageRef = storage.reference
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            var file = Uri.fromFile(File(path))
            val riversRef = storageRef.child("images/${uid}.png")
            riversRef.putFile(file)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        val imageUrl = it.toString()
                        userInfo.avatar = imageUrl
                        completeBlock(true, imageUrl)
                    }
                }

                ?.addOnFailureListener(OnFailureListener { e ->
                    print(e.message)
                    completeBlock(false, null)
                })
        }
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