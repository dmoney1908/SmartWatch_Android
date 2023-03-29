package com.linhua.smartwatch.fragment

import android.content.Intent
import android.graphics.drawable.GradientDrawable
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
import com.linhua.smartwatch.met.MetActivity
import com.linhua.smartwatch.sleep.SleepActivity
import com.linhua.smartwatch.utils.DeviceManager
import com.linhua.smartwatch.utils.IntentUtil
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.zhj.bluetooth.zhjbluetoothsdk.bean.Goal
import com.zhj.bluetooth.zhjbluetoothsdk.bean.HealthHeartRate
import com.zhj.bluetooth.zhjbluetoothsdk.bean.HealthSport
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil.showToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment: BaseFragment(){
    private val RESULT_CODE_ADD = 1000
    var hostView: View? = null
    private var refreshLayout: RefreshLayout? = null
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

        }

        hostView?.findViewById<RelativeLayout>(R.id.rl_tempr)?.setOnClickListener {

        }

        hostView?.findViewById<RelativeLayout>(R.id.rl_weight)?.setOnClickListener {

        }

        hostView?.findViewById<RelativeLayout>(R.id.rl_blood_pressure)?.setOnClickListener {
            this.context?.let { it ->
                IntentUtil.goToActivity(
                    it,
                    BPActivity::class.java
                )
            }
        }
        if (!EventBus.getDefault().isRegistered(this))  //这里的取反别忘记了
            EventBus.getDefault().register(this)
        return hostView
    }

    override fun initData() {
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
        getTarget()
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
                hostView!!.findViewById<TextView>(R.id.tv_steps).setText(sport.totalStepCount.toString())
                hostView!!.findViewById<TextView>(R.id.tv_calories_value).setText((sport.totalCalory * 1000).toString()) //单位千卡
                hostView!!.findViewById<TextView>(R.id.tv_distance_value).setText(sport.totalDistance.toString()) //单位米
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
                refreshLayout!!.finishRefresh(true)
                val heartRate = handlerBleDataResult.data as HealthHeartRate
                hostView!!.findViewById<TextView>(R.id.tv_heart_rate_num).setText(heartRate.silentHeart.toString())
                hostView!!.findViewById<TextView>(R.id.tv_pressure_value).setText(heartRate.ss.toString())
            }

            override fun onFailed(e: WriteBleException) {
                refreshLayout!!.finishRefresh(true)
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            MessageEvent.DeviceStatusChanged -> {
                reloadView()
            }
        }
    }
}