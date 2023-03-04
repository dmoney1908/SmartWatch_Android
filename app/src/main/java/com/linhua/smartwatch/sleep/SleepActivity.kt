package com.linhua.smartwatch.sleep

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseActivity
import com.linhua.smartwatch.monthpicker.MonthYearPickerDialogFragment
import com.linhua.smartwatch.utils.DateType
import com.linhua.smartwatch.utils.DateUtil
import com.linhua.smartwatch.view.ScrollDateView
import com.lxj.xpopup.XPopup
import java.util.*

class SleepActivity : BaseActivity(), OnChartValueSelectedListener {
    private var dateType = DateType.Days
    private var currentMonth = Date()
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
        findViewById<ScrollDateView>(R.id.rl_scroll).selectCallBack = { date : Date ->
            selectDate(date)
        }
        findViewById<RelativeLayout>(R.id.rl_month).setOnClickListener {
            val calendar = Calendar.getInstance()
            val todayYear = calendar[Calendar.YEAR]
            val todayMonth = calendar[Calendar.MONTH]
            calendar.time = currentMonth
            val yearSelected = calendar[Calendar.YEAR]
            val monthSelected = calendar[Calendar.MONTH]
            MonthYearPickerDialogFragment.getInstance(monthSelected, yearSelected)
            calendar.clear()
            calendar.set(2022,1,1)
            val minDate = calendar.timeInMillis // Get milliseconds of the modified date
            calendar.clear()
            calendar.set(todayYear,todayMonth,1)
            val maxDate = calendar.timeInMillis
            val dialogFragment = MonthYearPickerDialogFragment.getInstance(monthSelected, yearSelected, minDate, maxDate)
            dialogFragment.setOnDateSetListener { year, monthOfYear ->
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                findViewById<TextView>(R.id.tv_time).text = DateUtil.getYMDate(calendar.time)
                currentMonth = calendar.time
            }

            dialogFragment.show(supportFragmentManager, null)
        }
    }

    private fun selectDate(date: Date) {

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