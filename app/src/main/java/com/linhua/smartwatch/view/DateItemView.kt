package com.linhua.smartwatch.view

import android.content.Context
import android.widget.LinearLayout
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.linhua.smartwatch.R
import java.util.*

class DateItemView(context: Context) : LinearLayout(context), View.OnClickListener {
    var date: Date? = null
    var week: Int? = null
    var day: Int? = null
    var selectCallBack: (day : Int, date: Date) -> Unit = { _: Int, _: Date -> }
    private var weekText: TextView? = null
    private var dayText: TextView? = null
    private var fillMode: FillMode = FillMode.Gray
    private var container: LinearLayout? = null
    private var weeks = listOf("S", "M","T", "W", "T", "F", "S")
    enum class FillMode {
        Gray,
        Normal,
        Highlight
    }
    init {
        var view = LayoutInflater.from(this.context).inflate(R.layout.item_date_view, this, true)
        weekText = view.findViewById(R.id.week);
        dayText = view.findViewById(R.id.day);
        container = view.findViewById(R.id.ll_container)
        view.alpha = 0.3F
    }

    fun updateUI(){
        if (week != null && week!! < weeks.count()) {
            weekText!!.text = weeks[week!!]
        }
        if (day != null) {
            dayText!!.text = day.toString()
        }
        setFillMode(FillMode.Gray)
    }

    fun setFillMode(mode: FillMode) {
        fillMode = mode
        when (fillMode) {
            FillMode.Gray -> {
                var drawable = container?.background as GradientDrawable
                drawable.setColor(Color.WHITE)

                weekText?.setTextColor(resources.getColor(R.color.primary_black))
                dayText?.setTextColor(resources.getColor(R.color.primary_black))
                container!!.alpha = 0.3F
            }
            FillMode.Normal -> {
                var drawable = container?.background as GradientDrawable
                drawable.setColor(Color.WHITE)
                weekText?.setTextColor(resources.getColor(R.color.primary_black))
                dayText?.setTextColor(resources.getColor(R.color.primary_black))

                container!!.alpha = 1.0F
            }
            FillMode.Highlight -> {
                var drawable = container?.background as GradientDrawable
                drawable.setColor(resources.getColor(R.color.primary_blue))
                weekText?.setTextColor(Color.WHITE)
                weekText?.alpha = 0.5f
                dayText?.setTextColor(Color.WHITE)
                container!!.alpha = 1.0F
            }
        }

    }

    override fun onClick(p0: View?) {
        if (p0 != null && p0.id == R.layout.item_date_view) {
            selectCallBack(day!!, date!!)
        }
    }
}

