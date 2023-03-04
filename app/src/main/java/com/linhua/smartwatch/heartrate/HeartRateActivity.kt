package com.linhua.smartwatch.heartrate

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseActivity
import com.linhua.smartwatch.utils.DateType
import com.linhua.smartwatch.utils.DateUtil
import com.linhua.smartwatch.view.ScrollDateView
import com.lxj.xpopup.XPopup
import java.util.*



class HeartRateActivity : BaseActivity(), OnChartValueSelectedListener {
    private var dateType = DateType.Days

    override fun initData() {
        findViewById<TextView>(R.id.tv_time).text = DateUtil.getYMDate(Date())
        findViewById<View>(R.id.v_date_type).setOnClickListener {
            XPopup.Builder(this).atView(findViewById<View>(R.id.v_date_type)).asAttachList(
                arrayOf("Days", "Weeks", "Months"),
                null
            ) { _, text ->
                dateType = DateType.valueOf(text)
                findViewById<TextView>(R.id.iv_date_type).text = text
            }.show()
        }
        findViewById<ScrollDateView>(R.id.rl_scroll).selectCallBack = {  date : Date ->
            selectDate(date)
        }
    }

    private fun selectDate(date: Date) {

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_heart_rate
    }

    override fun onListener() {
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        TODO("Not yet implemented")
    }

    override fun onNothingSelected() {
        TODO("Not yet implemented")
    }
}