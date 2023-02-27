package com.linhua.smartwatch.heartrate

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseActivity
import com.linhua.smartwatch.chart.DemoBase

class HeartRateActivity : BaseActivity(), OnChartValueSelectedListener {


    override fun initData() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_devices
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