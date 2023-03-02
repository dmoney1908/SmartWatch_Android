package com.linhua.smartwatch.activity

import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseActivity
import com.linhua.smartwatch.fragment.DeviceFragment
import com.linhua.smartwatch.fragment.HomeFragment
import com.linhua.smartwatch.fragment.PersonalFragment
import com.linhua.smartwatch.fragment.SportFragment
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
    private lateinit var deviceFragment: DeviceFragment
    private lateinit var personalFragment: PersonalFragment
    private lateinit var homeFragment: HomeFragment
    private lateinit var sportFragment: SportFragment

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onListener() {
        val bottomView = findViewById<BottomNavigationView>(R.id.bottom_view)
        bottomView.setOnItemSelectedListener(this)
        bottomView.selectedItemId = R.id.navigation_home
        var test = BluetoothLe.getDefault()
        BluetoothLe.getDefault().init(this, object : BleCallbackWrapper() {

            override fun complete(resultCode: Int, data: Any?) {
                LogUtil.d("resultCode:$resultCode")
                if (resultCode == Constants.BLE_RESULT_CODE.SUCCESS) {
                } else {
                    LogUtil.d("SDK不能使用")
                }
            }

            override fun setSuccess() {
                LogUtil.d("SDK不能使用")
            }

        })
    }

    private fun prepareFragments() {
        homeFragment =
            supportFragmentManager.findFragmentByTag(TAG_HOME) as HomeFragment?
                ?: HomeFragment()
        sportFragment =
            supportFragmentManager.findFragmentByTag(TAG_SPORT) as SportFragment?
                ?: SportFragment()
        deviceFragment =
            supportFragmentManager.findFragmentByTag(TAG_DEVICE) as DeviceFragment?
                ?: DeviceFragment()
        personalFragment =
            supportFragmentManager.findFragmentByTag(TAG_MINE) as PersonalFragment?
                ?: PersonalFragment()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val transAction = supportFragmentManager.beginTransaction()
        transAction.replace(R.id.content_container,
            FragmentManage.fragmentManage.getFragmentById(item.itemId)!!,item.itemId.toString())
        transAction.commit()
        return true
    }
}