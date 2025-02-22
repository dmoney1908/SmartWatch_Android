package com.linhua.smartwatch.utils

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Base64
import com.linhua.smartwatch.SmartWatchApplication
import com.linhua.smartwatch.event.MessageEvent
import com.linhua.smartwatch.helper.UserData
import com.zhj.bluetooth.zhjbluetoothsdk.bean.BLEDevice
import org.greenrobot.eventbus.EventBus
import java.io.*


object DeviceManager {
    var isSDKAvailable = false
    private var currentDevice: BLEDevice? = null
    private var deviceList: MutableList<BLEDevice>? = null

    init {
        deviceList = mutableListOf<BLEDevice>()
        loadDevices()
    }

    fun getCurrentDevice(): BLEDevice? {
        if (deviceList!!.isEmpty())return null
        return if (currentDevice == null) {
            deviceList!!.first()
        } else {
            currentDevice
        }
    }

    fun getConnectedDevice(): BLEDevice? {
        return currentDevice
    }

    fun setConnectedDevice(device: BLEDevice?) {
        currentDevice = device
        UserData.saveMac(device?.mDeviceAddress)
        val event = MessageEvent(MessageEvent.DeviceStatusChanged)
        EventBus.getDefault().post(event)
    }

    fun getDeviceList():List<BLEDevice> {
        return deviceList!!
    }

    fun addDevice(device: BLEDevice) {
        if (!isContainDevice(device)) {
            deviceList!!.add(device)
            saveDevices()
        }
    }

    fun isContainDevice(device: BLEDevice): Boolean {
        if (deviceList == null) {
            return false
        } else {
            return deviceList!!.contains(device)
        }
    }

    fun removeDevice(position: Int) {
        deviceList!!.removeAt(position)
        saveDevices()
    }

    fun saveDevices() {
        val devicesSP: SharedPreferences = SmartWatchApplication.instance.getSharedPreferences("devices", MODE_PRIVATE)
        try {
            val var2 = ByteArrayOutputStream()
            var var3: ObjectOutputStream? = null
            var3 = ObjectOutputStream(var2)
            var3.writeObject(deviceList!!)
            val var4 = String(Base64.encode(var2.toByteArray(), 0))
            var3.close()
            devicesSP.edit().putString("devices", var4).apply()
        } catch (var5: IOException) {
            var5.printStackTrace()
        }
    }

    fun loadDevices() {
        deviceList?.clear()
        val devicesSP: SharedPreferences = SmartWatchApplication.instance.getSharedPreferences("devices", MODE_PRIVATE)

        try {
            var var2 = devicesSP.getString("devices", "")
            if (var2 == null || var2.isEmpty()) {
                return
            }
            val var3 = Base64.decode(var2.toByteArray(), 0)
            val var4 = ByteArrayInputStream(var3)
            val var5 = ObjectInputStream(var4)
            val var1 = var5.readObject() as List<BLEDevice>
            var5.close()
            for (item in var1) {
                if (!isContainDevice(item)) {
                    deviceList?.add(item)
                }
            }
        } catch (var6: ClassNotFoundException) {
            var6.printStackTrace()
        } catch (var6: IOException) {
            var6.printStackTrace()
        }
    }


}