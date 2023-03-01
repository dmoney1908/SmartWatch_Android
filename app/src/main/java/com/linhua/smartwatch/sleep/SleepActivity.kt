package com.linhua.smartwatch.sleep

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseActivity

class SleepActivity : BaseActivity(), OnChartValueSelectedListener {

    override fun initData() {

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_sleep
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