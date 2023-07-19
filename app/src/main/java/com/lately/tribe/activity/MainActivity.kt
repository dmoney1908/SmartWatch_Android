package com.lately.tribe.activity

import android.bluetooth.BluetoothGatt
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.lately.tribe.R
import com.lately.tribe.base.BaseActivity
import com.lately.tribe.helper.UserData
import com.lately.tribe.utils.CommonUtil
import com.lately.tribe.utils.DeviceManager
import com.lately.tribe.utils.FragmentManage
import com.scwang.smart.refresh.header.ClassicsHeader
import com.zhj.bluetooth.zhjbluetoothsdk.bean.BLEDevice
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleCallbackWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.BluetoothLe
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeConnectListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.ConnBleException
import com.zhj.bluetooth.zhjbluetoothsdk.util.Constants
import com.zhj.bluetooth.zhjbluetoothsdk.util.LogUtil

class MainActivity : BaseActivity(), NavigationBarView.OnItemSelectedListener   {
    companion object {
        const val TAG_HOME = "home"
        const val TAG_DEVICE = "devices"
        const val TAG_SPORT = "sport"
        const val TAG_MINE = "mine"
    }
    var bottomView: BottomNavigationView? = null
    private var currentFragment: Fragment? = null
    private var connectDevice: BLEDevice? = null
    var mBluetoothLe: BluetoothLe? = null

    override fun prepareData() {
        ClassicsHeader.REFRESH_HEADER_PULLING = getString(R.string.header_pulling)
        ClassicsHeader.REFRESH_HEADER_REFRESHING = getString(R.string.header_refreshing)
        ClassicsHeader.REFRESH_HEADER_LOADING = getString(R.string.header_loading)
        ClassicsHeader.REFRESH_HEADER_RELEASE = getString(R.string.header_release)
        ClassicsHeader.REFRESH_HEADER_FINISH = getString(R.string.header_finish)
        ClassicsHeader.REFRESH_HEADER_FAILED = getString(R.string.header_failed)
        ClassicsHeader.REFRESH_HEADER_SECONDARY = getString(R.string.header_secondary)
        ClassicsHeader.REFRESH_HEADER_UPDATE = getString(R.string.header_update)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onListener() {
        bottomView = findViewById<BottomNavigationView>(R.id.bottom_view)
        bottomView?.setOnItemSelectedListener(this)
        bottomView?.selectedItemId = R.id.navigation_home
        bottomView?.itemIconTintList = null
        BluetoothLe.getDefault().init(this, object : BleCallbackWrapper() {

            override fun complete(resultCode: Int, data: Any?) {
                LogUtil.d("resultCode:$resultCode")
                if (resultCode == Constants.BLE_RESULT_CODE.SUCCESS) {
                    DeviceManager.isSDKAvailable = true
                    autoConnect()
                } else {
                    DeviceManager.isSDKAvailable = false
                    showToast(resources.getString(R.string.sdk_not_available))
                    LogUtil.d("SDK不能使用")
                }
            }

            override fun setSuccess() {

            }

        })
    }

    private fun autoConnect() {
        if (!DeviceManager.isSDKAvailable) {
            return
        }
        mBluetoothLe = BluetoothLe.getDefault()
        if (!mBluetoothLe!!.isBluetoothOpen || !CommonUtil.isOPen(this)) {
            return
        }
        if (DeviceManager.getConnectedDevice() == null){
            val devices = DeviceManager.getDeviceList()
            if (UserData.lastMac.isNotEmpty() && devices.isNotEmpty()) {
                for (item in devices) {
                    if (item.mDeviceAddress == UserData.lastMac) {
                        showLoading()
                        addConnectionListener()
                        connectDevice = item
                        mBluetoothLe?.startConnect(item.mDeviceAddress)
                        return
                    }
                }
            }
        }
    }

    private fun addConnectionListener() {
        mBluetoothLe!!.setOnConnectListener(TAG, object : OnLeConnectListener() {
            override fun onDeviceConnecting() {}
            override fun onDeviceConnected() {}
            override fun onDeviceDisconnected() {
                connectDevice = null
                hideLoading()
            }
            
            override fun onServicesDiscovered(bluetoothGatt: BluetoothGatt) {
                if (connectDevice != null) {
                    DeviceManager.setConnectedDevice(connectDevice)
                    DeviceManager.addDevice(connectDevice!!)
                    mBluetoothLe!!.destroy(TAG)
                    hideLoading()
                }
            }

            override fun onDeviceConnectFail(e: ConnBleException) {
                connectDevice = null
                mBluetoothLe!!.destroy(TAG)
                hideLoading()
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val fragment = FragmentManage.fragmentManage.getFragmentById(item.itemId)
        if (fragment == currentFragment) {
            return true
        }
        currentFragment?.apply {
            safeCommit(supportFragmentManager.beginTransaction().hide(this))
        }
        val transAction = supportFragmentManager.beginTransaction()
        if (fragment!!.isAdded) {
            transAction.show(fragment)
        } else {
            transAction.add(R.id.content_container,
                fragment,item.itemId.toString())
        }
        currentFragment = fragment
        safeCommit(transAction)
        return true
    }

    private fun safeCommit(transaction: FragmentTransaction) {
        try {
            supportFragmentManager.executePendingTransactions()
            transaction.commit()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectDevice = null
        mBluetoothLe?.destroy(TAG)
    }
}