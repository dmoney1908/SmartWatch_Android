package com.linhua.smartwatch.fragment

import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ColorUtils
import com.linhua.smartwatch.R
import com.linhua.smartwatch.activity.MainActivity
import com.linhua.smartwatch.activity.ScanDeviceReadyActivity
import com.linhua.smartwatch.base.BaseFragment
import com.linhua.smartwatch.bp.BPActivity
import com.linhua.smartwatch.event.MessageEvent
import com.linhua.smartwatch.heartrate.HeartRateActivity
import com.linhua.smartwatch.helper.UserData
import com.linhua.smartwatch.met.MetActivity
import com.linhua.smartwatch.mine.PersonalInfoActivity
import com.linhua.smartwatch.oxygen.OxygenActivity
import com.linhua.smartwatch.sleep.SleepActivity
import com.linhua.smartwatch.tempr.TemperatureActivity
import com.linhua.smartwatch.utils.CommonUtil
import com.linhua.smartwatch.utils.CommonUtil.AutoTempr
import com.linhua.smartwatch.utils.DateUtil
import com.linhua.smartwatch.utils.DeviceManager
import com.linhua.smartwatch.utils.IntentUtil
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.zhj.bluetooth.zhjbluetoothsdk.bean.*
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.BluetoothLe
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeConnectListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.ConnBleException
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil.showToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class HomeFragment: BaseFragment(){
    private val RESULT_CODE_ADD = 1000
    var hostView: View? = null
    private var refreshLayout: RefreshLayout? = null

    private var dailyDateIndex = 0
    protected val TAG: String = this.javaClass.simpleName
    private var healthSleepItems = mutableListOf<List<HealthSleepItem>?>()
    private var todayCalendar = Calendar.getInstance()
    private var totalSteps: Int? = null
    private var tmpHandler: Int? = null

    override fun initView(): View? {
        hostView = View.inflate(activity, R.layout.fragment_home,null) as View?
        hostView?.findViewById<ImageView>(R.id.iv_add)?.setOnClickListener {
            this.context?.let { it ->
                IntentUtil.goToActivityForResult(
                    it,
                    ScanDeviceReadyActivity::class.java,
                    RESULT_CODE_ADD
                )
            }
        }

        refreshLayout = hostView?.findViewById(R.id.refreshLayout) as RefreshLayout
        refreshLayout!!.setRefreshHeader(ClassicsHeader(this.activity))

        refreshLayout!!.apply {
            setOnRefreshListener {
                reloadView()
            }
        }

        hostView?.findViewById<RelativeLayout>(R.id.rl_heart_rate)?.setOnClickListener {
            this.context?.let { it ->
                IntentUtil.goToActivity(
                    it,
                    HeartRateActivity::class.java
                )
            }
        }

        hostView?.findViewById<RelativeLayout>(R.id.rl_sleep)?.setOnClickListener {
            this.context?.let { it ->
                IntentUtil.goToActivity(
                    it,
                    SleepActivity::class.java
                )
            }
        }

        hostView?.findViewById<RelativeLayout>(R.id.rl_met)?.setOnClickListener {
            this.context?.let { it ->
                IntentUtil.goToActivity(
                    it,
                    MetActivity::class.java
                )
            }
        }

        hostView?.findViewById<RelativeLayout>(R.id.rl_blood_oxygen)?.setOnClickListener {
            this.context?.let { it ->
                IntentUtil.goToActivity(
                    it,
                    OxygenActivity::class.java
                )
            }
        }

        hostView?.findViewById<RelativeLayout>(R.id.rl_tempr)?.setOnClickListener {
            this.context?.let { it ->
                IntentUtil.goToActivity(
                    it,
                    TemperatureActivity::class.java
                )
            }
        }

        hostView?.findViewById<RelativeLayout>(R.id.rl_weight)?.setOnClickListener {
            this.context?.let { it ->
                IntentUtil.goToActivity(
                    it,
                    PersonalInfoActivity::class.java
                )
            }
        }

        hostView?.findViewById<RelativeLayout>(R.id.rl_blood_pressure)?.setOnClickListener {
            this.context?.let { it ->
                IntentUtil.goToActivity(
                    it,
                    BPActivity::class.java
                )
            }
        }
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
        UserData.syncSystemSettings {
            reloadUnit()
        }
        return hostView
    }

    override fun initData() {
        UserData.fetchTribe {  }
    }
    override fun onListener() {
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        loadStatus()
    }

    private fun loadStatus() {
        hostView!!.findViewById<TextView>(R.id.tv_name).text = DeviceManager.getCurrentDevice()?.mDeviceName ?: "FireBoltt"
        hostView!!.findViewById<TextView>(R.id.tv_name).text = DeviceManager.getCurrentDevice()?.mDeviceName ?: "FireBoltt"
        var connectedDevice = DeviceManager.getConnectedDevice()
        var drawable = hostView!!.findViewById<View>(R.id.v_status).background as GradientDrawable

        if (connectedDevice != null) {
            drawable.setColor(ColorUtils.getColor(R.color.green))
            hostView!!.findViewById<TextView>(R.id.tv_status).text = requireActivity().resources.getString(R.string.connected_status)
            hostView!!.findViewById<TextView>(R.id.tv_status).setTextColor(ColorUtils.getColor(R.color.green))
        } else {
            drawable.setColor(ColorUtils.getColor(R.color.red))
            hostView!!.findViewById<TextView>(R.id.tv_status).text = requireActivity().resources.getString(R.string.disconnected_status)
            hostView!!.findViewById<TextView>(R.id.tv_status).setTextColor(ColorUtils.getColor(R.color.red))
        }
    }

    private fun reloadView() {
        loadStatus()
        if (!DeviceManager.isSDKAvailable) {
            showToast(activity, resources.getString(R.string.sdk_not_available))
            refreshLayout!!.finishRefresh(false)
            return
        }
        if (DeviceManager.getConnectedDevice() == null){
            refreshLayout!!.finishRefresh(true)
            return
        }
        syncTime()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_CODE_ADD-> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    val mainActivity = activity as MainActivity
                    mainActivity.bottomView?.selectedItemId = R.id.navigation_device
                }
            }
        }
    }

    private fun syncTime() {
        BleSdkWrapper.setDeviceData(object :OnLeWriteCharacteristicListener() {
            override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                syncUnit()
            }

            override fun onFailed(e: WriteBleException) {
                syncUnit()
            }
        })
    }

    private fun syncUnit() {
        val deviceState = DeviceState()
        deviceState.tempUnit = if (UserData.systemSetting.temprUnit == 1) 0 else 1
        deviceState.unit = if (UserData.systemSetting.unitSettings == 1) 0 else 1
        deviceState.timeFormat = 1
        BleSdkWrapper.setDeviceState(deviceState, object : OnLeWriteCharacteristicListener() {
            override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                getTarget()
            }

            override fun onFailed(e: WriteBleException) {
                getTarget()
            }
        })
    }

    private fun getTarget() {
        BleSdkWrapper.getTarget(object : OnLeWriteCharacteristicListener() {
            override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                val goal = handlerBleDataResult.data as Goal
                hostView!!.findViewById<TextView>(R.id.tv_goal).setText(goal.goalStep.toString()) //单位步
                getCurrentStep()
            }

            override fun onFailed(e: WriteBleException) {
                getCurrentStep()
            }
        })
    }

    private fun getCurrentStep() {
        BleSdkWrapper.getCurrentStep(object : OnLeWriteCharacteristicListener() {
            override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                val sport = handlerBleDataResult.data as HealthSport
                hostView!!.findViewById<TextView>(R.id.tv_steps).text = sport.totalStepCount.toString()
                hostView!!.findViewById<TextView>(R.id.tv_calories_value).text = (sport.totalCalory).toString() //单位千卡
                hostView!!.findViewById<TextView>(R.id.tv_distance_value).text = sport.totalDistance.toString() //单位米
                totalSteps = sport.totalStepCount
                UserData.healthData.date = DateUtil.getYMDHMDate(Date()).toString()
                UserData.healthData.steps = sport.totalStepCount
                if (UserData.systemSetting.unitSettings == 1) {
                    if (UserData.userInfo.sex == 1) {
                        hostView!!.findViewById<TextView>(R.id.tv_distance_value).text =
                            String.format("%.1f", sport.totalStepCount / 1000.0 * 0.6096)
                    } else {
                        hostView!!.findViewById<TextView>(R.id.tv_distance_value).text =
                            String.format("%.1f", sport.totalStepCount / 1000.0 * 0.762) //单位km
                    }
                    hostView!!.findViewById<TextView>(R.id.tv_distance_unit).text = "km"
                } else {
                    if (UserData.userInfo.sex == 1) {
                        hostView!!.findViewById<TextView>(R.id.tv_distance_value).text =
                            String.format("%.1f", sport.totalStepCount / 1609.0 * 0.6096)
                    } else {
                        hostView!!.findViewById<TextView>(R.id.tv_distance_value).text =
                            String.format("%.1f", sport.totalStepCount / 1609.0 * 0.762) //单位mile
                    }
                    hostView!!.findViewById<TextView>(R.id.tv_distance_unit).text = "miles"
                }

                getCurrentRate()
            }

            override fun onFailed(e: WriteBleException) {
                getCurrentRate()
            }
        })
    }

    private fun getCurrentRate() {
        BleSdkWrapper.getHeartRate(object : OnLeWriteCharacteristicListener() {
            override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {

                val heartRate = handlerBleDataResult.data as HealthHeartRate
                hostView!!.findViewById<TextView>(R.id.tv_heart_rate_num).text = heartRate.silentHeart.toString()
                hostView!!.findViewById<TextView>(R.id.tv_pressure_value).text = heartRate.ss.toString()
                hostView!!.findViewById<TextView>(R.id.tv_oxygen_num).text = heartRate.oxygen.toString() + "%"
                dailyDateIndex = 0
                todayCalendar = Calendar.getInstance()
                syncDailySleepHistory()
            }

            override fun onFailed(e: WriteBleException) {
                syncDailySleepHistory()
            }
        })
    }

    private fun getCurrentMetInfo() {
        BleSdkWrapper.getMettInfo(object : OnLeWriteCharacteristicListener() {
            override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                refreshLayout!!.finishRefresh(true)
                val map = handlerBleDataResult.data as Map<Int, Int>
                val met = map[0]
                hostView!!.findViewById<TextView>(R.id.tv_met_num).text = met.toString()
            }

            override fun onFailed(p0: WriteBleException?) {
                refreshLayout!!.finishRefresh(true)
            }
        })
    }

    //历史睡眠数据 (正常计算卡路里数据)
    private fun syncDailySleepHistory() {
        dailyDateIndex++
        val year: Int = todayCalendar.get(Calendar.YEAR)
        val month: Int = todayCalendar.get(Calendar.MONTH) + 1
        val day: Int = todayCalendar.get(Calendar.DATE)
        BleSdkWrapper.getStepOrSleepHistory(
            year,
            month,
            day,
            object : OnLeWriteCharacteristicListener() {
                override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                    if (handlerBleDataResult.isComplete) {
                        if (handlerBleDataResult.hasNext) {
                            val sleepItems = handlerBleDataResult.sleepItems
                            if (sleepItems != null) {
                                healthSleepItems.add(sleepItems)
                            }
                            if (dailyDateIndex >= 2) {
                                showSleepData()
                                return
                            }
                            todayCalendar.add(Calendar.DATE, -1)
                            syncDailySleepHistory()

                        } else {
                            showSleepData()
                        }
                    }else {
                        getCurrentTmp()
                    }
                }

                override fun onFailed(e: WriteBleException) {
                    getCurrentTmp()
                }
            })
    }

    private fun showSleepData() {
        if (healthSleepItems.isEmpty()) return
        val item = computeMath()
        UserData.healthData.sleepTime = item
        UserData.healthData.date = DateUtil.getYMDHMDate(Date()).toString()
        UserData.updateTribeDetail{}
        val time = item / 60.0
        hostView!!.findViewById<TextView>(R.id.tv_sleep_num).text = String.format("%.1f", time)
        getCurrentTmp()
    }

    private fun computeMath(): Int {
        var deep = 0
        var light = 0
        var wide = 0
        if (healthSleepItems.isEmpty()) {
            return 0
        }
        val sleepItems = healthSleepItems.first()

        for (item in sleepItems!!) {
            if (item.sleepStatus == 2) {
                light += 10
            } else if (item.sleepStatus == 3) {
                deep += 10
            } else if (item.sleepStatus == 4) {
                wide += 10
            }
        }
        return deep + light + wide
    }

    private fun getCurrentTmp() {
        BleSdkWrapper.getCurrentTmp(object : OnLeWriteCharacteristicListener() {
            override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                val tempInfo = handlerBleDataResult.data as TempInfo
                tmpHandler = tempInfo.tmpHandler
                hostView!!.findViewById<TextView>(R.id.tv_tempr_value).text = String.format("%.1f", tempInfo.tmpHandler / 100.0)
                getCurrentMetInfo()
            }

            override fun onFailed(p0: WriteBleException?) {
                getCurrentMetInfo()
            }
        })
    }

    private fun reloadUnit() {
        if (totalSteps != null) {
            if (UserData.systemSetting.unitSettings == 1) {
                if (UserData.userInfo.sex == 1) {
                    hostView!!.findViewById<TextView>(R.id.tv_distance_value).text =
                        String.format("%.1f", totalSteps!! / 1000.0 * 0.6096)
                } else {
                    hostView!!.findViewById<TextView>(R.id.tv_distance_value).text =
                        String.format("%.1f", totalSteps!! / 1000.0 * 0.762) //单位km
                }
                hostView!!.findViewById<TextView>(R.id.tv_distance_unit).text = requireActivity().resources.getString(R.string.km)
            } else {
                if (UserData.userInfo.sex == 1) {
                    hostView!!.findViewById<TextView>(R.id.tv_distance_value).text =
                        String.format("%.1f", totalSteps!! / 1609.0 * 0.6096)
                } else {
                    hostView!!.findViewById<TextView>(R.id.tv_distance_value).text =
                        String.format("%.1f", totalSteps!! / 1609.0 * 0.762) //单位mile
                }
                hostView!!.findViewById<TextView>(R.id.tv_distance_unit).text = requireActivity().resources.getString(R.string.miles)
            }
        } else {
            if (UserData.systemSetting.unitSettings == 1) {
                hostView!!.findViewById<TextView>(R.id.tv_distance_unit).text = requireActivity().resources.getString(R.string.km)
            } else {
                hostView!!.findViewById<TextView>(R.id.tv_distance_unit).text = requireActivity().resources.getString(R.string.miles)
            }
        }
        if (UserData.systemSetting.temprUnit == 0) {
            hostView!!.findViewById<TextView>(R.id.tv_tempr_unit).text = requireActivity().resources.getString(R.string.tempr_f)
        } else {
            hostView!!.findViewById<TextView>(R.id.tv_tempr_unit).text = requireActivity().resources.getString(R.string.tempr_c)
        }

        if (tmpHandler != null) {
            hostView!!.findViewById<TextView>(R.id.tv_tempr_value).text = String.format("%.1f", AutoTempr((tmpHandler!! / 100.0).toFloat()))
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageEvent.DeviceStatusChanged -> {
                reloadView()
            }
            MessageEvent.UnitChanged -> {
                reloadUnit()
            }
        }
    }
}