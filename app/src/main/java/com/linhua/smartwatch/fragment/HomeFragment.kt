package com.linhua.smartwatch.fragment

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.linhua.smartwatch.R
import com.linhua.smartwatch.activity.MainActivity
import com.linhua.smartwatch.activity.ScanDeviceReadyActivity
import com.linhua.smartwatch.base.BaseFragment
import com.linhua.smartwatch.bp.BPActivity
import com.linhua.smartwatch.heartrate.HeartRateActivity
import com.linhua.smartwatch.sleep.SleepActivity
import com.linhua.smartwatch.utils.IntentUtil

class HomeFragment: BaseFragment(){
    private val RESULT_CODE_ADD = 1000
    override fun initView(): View? {
        val hostView = View.inflate(activity, R.layout.fragment_home,null) as View?
        hostView?.findViewById<ImageView>(R.id.iv_add)?.setOnClickListener {
            this.context?.let { it ->
                IntentUtil.goToActivityForResult(
                    it,
                    ScanDeviceReadyActivity::class.java,
                    RESULT_CODE_ADD
                )
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

        }

        hostView?.findViewById<RelativeLayout>(R.id.rl_blood_pressure)?.setOnClickListener {
            this.context?.let { it ->
                IntentUtil.goToActivity(
                    it,
                    BPActivity::class.java
                )
            }
        }

        return hostView
    }

    override fun initData() {
    }
    override fun onListener() {
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
}