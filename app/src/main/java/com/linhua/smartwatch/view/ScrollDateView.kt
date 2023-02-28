package com.linhua.smartwatch.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.linhua.smartwatch.R
import com.linhua.smartwatch.utils.DateUtil
import java.util.*


class ScrollDateView(context: Context, attrs: AttributeSet?) : HorizontalScrollView(context, attrs) {
    var currentMonth: Date? = null
    var selectedIndex:Int = 0
    var container: LinearLayout? = null
    var calendar: Calendar? = null
    private var dateItemList = mutableListOf<DateItemView>()

    init {
        var view = LayoutInflater.from(this.context).inflate(R.layout.item_date_view, this, true)
        container = view.findViewById(R.id.ll_container);

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
            container?.addView(dateItem)
            dateItemList.add(dateItem)
            dateItem.selectCallBack = { day: Int, _: Date ->
                selectDayAction(day)
            }

            dateItem.date = calendar!!.time
            dateItem.day = i + 1
            dateItem.week = DateUtil.computeWeekNum(year, month, i + 1)
            dateItem.updateUI()
            if (selectDay == i) {
                dateItem.setFillMode(DateItemView.FillMode.Highlight)
                dateItem.alpha = 1f
            } else if(selectDay == i - 1 || selectDay == i + 1) {
                dateItem.setFillMode(DateItemView.FillMode.Normal)
                dateItem.alpha = 1f
            } else if(selectDay == i - 2 || selectDay == i + 2) {
                dateItem.setFillMode(DateItemView.FillMode.Gray)
                dateItem.alpha = 0.7F
            } else {
                dateItem.setFillMode(DateItemView.FillMode.Gray)
                dateItem.alpha = 0.3F
            }
        }
        this.smoothScrollTo(selectedIndex * 48, 0)
    }

    fun selectDayAction(selectDay: Int) {
        for (i in 0 until dateItemList.count()) {
            val dateItem = dateItemList[i]
            if (selectDay == i) {
                dateItem.setFillMode(DateItemView.FillMode.Highlight)
                dateItem.alpha = 1f
            } else if(selectDay == i - 1 || selectDay == i + 1) {
                dateItem.setFillMode(DateItemView.FillMode.Normal)
                dateItem.alpha = 1f
            } else if(selectDay == i - 2 || selectDay == i + 2) {
                dateItem.setFillMode(DateItemView.FillMode.Gray)
                dateItem.alpha = 0.7F
            } else {
                dateItem.setFillMode(DateItemView.FillMode.Gray)
                dateItem.alpha = 0.3F
            }
        }
        this.smoothScrollTo(selectedIndex * 48, 0)
    }


}