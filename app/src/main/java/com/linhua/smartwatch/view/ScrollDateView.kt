package com.linhua.smartwatch.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.linhua.smartwatch.R
import com.linhua.smartwatch.utils.DateUtil
import com.linhua.smartwatch.utils.ScreenUtil
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil
import java.util.*


class ScrollDateView(context: Context, attrs: AttributeSet?) : HorizontalScrollView(context, attrs) {
    var currentMonth: Date? = null
    var selectedIndex:Int = 0
    var container: LinearLayout? = null
    var calendar: Calendar? = null
    private var dateItemList = mutableListOf<DateItemView>()
//    var selectCallBack: (day : Int, date: Date) -> Unit = { _: Int, _: Date -> }
    var selectCallBack: (date: Date) -> Unit = { _: Date -> }

    init {
        var view = LayoutInflater.from(this.context).inflate(R.layout.scroll_date_view, this, true)
        container = view.findViewById(R.id.ll_container);
        updateMonthUI(Date())
    }

    fun updateMonthUI(date: Date){
        currentMonth = date
        setupViews()
    }

    fun setupViews() {
        container!!.removeAllViews()
        dateItemList.clear()
        if (currentMonth == null) {
            currentMonth = Date()
        }
        if (calendar == null) {
            calendar = Calendar.getInstance()
        }
        calendar!!.time = currentMonth
        val year: Int = calendar!!.get(Calendar.YEAR)
        val month: Int = calendar!!.get(Calendar.MONTH) + 1
        val day: Int = calendar!!.get(Calendar.DATE)
        val days = DateUtil.computeMonthDayCount(year, month)

        calendar!!.time = Date()
        var selectDay = days / 2
        if (calendar!!.get(Calendar.YEAR) == year && calendar!!.get(Calendar.MONTH) + 1 == month) {
            selectDay = calendar!!.get(Calendar.DATE)
        }
        for (i in 0 until days) {
            calendar!!.set(Calendar.DATE, i + 1)
            val dateItem = DateItemView(this.context)
            dateItem.setOnClickListener {
                val dateItem = it as? DateItemView
                if (dateItem != null) {
                    selectDayAction(dateItem.day!!, dateItem.date!!)
                }
            }
            container?.addView(dateItem)
            dateItemList.add(dateItem)
//            dateItem.selectCallBack = { day: Int, _: Date ->
//                selectDayAction(day)
//            }

            dateItem.date = calendar!!.time
            dateItem.day = i + 1
            dateItem.week = DateUtil.computeWeekNum(year, month, i + 1)
            dateItem.updateUI()
            if (selectDay == i + 1) {
                selectedIndex = i
                dateItem.setFillMode(DateItemView.FillMode.Highlight)
                dateItem.alpha = 1f
            } else if(selectDay == i || selectDay == i + 2) {
                dateItem.setFillMode(DateItemView.FillMode.Normal)
                dateItem.alpha = 1f
            } else if(selectDay == i - 1 || selectDay == i + 3) {
                dateItem.setFillMode(DateItemView.FillMode.Gray)
                dateItem.alpha = 0.7F
            } else {
                dateItem.setFillMode(DateItemView.FillMode.Gray)
                dateItem.alpha = 0.3F
            }

        }
        var itemWidth = ScreenUtil.dp2px(48f, context)
        var width = ScreenUtil.getScreenWidth(this.context)
        var offset = selectedIndex * itemWidth - width / 2 + itemWidth / 2
        if (offset < 0) offset = 0
        this.smoothScrollTo(offset, 0)
    }

    private fun selectDayAction(selectDay: Int, date: Date) {
        for (i in 0 until dateItemList.count()) {
            val dateItem = dateItemList[i]
            if (selectDay == i + 1) {
                selectedIndex = i
                var width = dateItem.measuredWidth
                dateItem.setFillMode(DateItemView.FillMode.Highlight)
                dateItem.alpha = 1f
            } else if(selectDay == i || selectDay == i + 2) {
                dateItem.setFillMode(DateItemView.FillMode.Normal)
                dateItem.alpha = 1f
            } else if(selectDay == i - 1 || selectDay == i + 3) {
                dateItem.setFillMode(DateItemView.FillMode.Gray)
                dateItem.alpha = 0.7F
            } else {
                dateItem.setFillMode(DateItemView.FillMode.Gray)
                dateItem.alpha = 0.3F
            }
        }
        var itemWidth = ScreenUtil.dp2px(48f, context)
        var width = ScreenUtil.getScreenWidth(this.context)
        var offset = selectedIndex * itemWidth - width / 2 + itemWidth / 2
        if (offset < 0) offset = 0
        this.smoothScrollTo(offset, 0)
        selectCallBack(date)
//        var b = resources.displayMetrics.widthPixels
//        var c = context.resources.displayMetrics.widthPixels

    }
}