package com.linhua.smartwatch.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseActivity
import com.linhua.smartwatch.fragment.DeviceFragment
import com.linhua.smartwatch.fragment.HomeFragment
import com.linhua.smartwatch.fragment.PersonalFragment
import com.linhua.smartwatch.fragment.SportFragment
import com.linhua.smartwatch.utils.DeviceManager
import com.linhua.smartwatch.utils.FragmentManage
import com.zhj.bluetooth.zhjbluetoothsdk.bean.WarningInfo
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleCallbackWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.BluetoothLe
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.DeviceCallback
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

    override fun prepareData() {
        setTheme(R.style.Theme_SmartWatch)
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
}