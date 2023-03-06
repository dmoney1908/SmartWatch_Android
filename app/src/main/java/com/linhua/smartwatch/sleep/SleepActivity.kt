package com.linhua.smartwatch.sleep

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ColorUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.Utils
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseActivity
import com.linhua.smartwatch.chart.MyMarkerView
import com.linhua.smartwatch.monthpicker.MonthYearPickerDialogFragment
import com.linhua.smartwatch.utils.DateType
import com.linhua.smartwatch.utils.DateUtil
import com.linhua.smartwatch.view.ScrollDateView
import com.lxj.xpopup.XPopup
import com.zhj.bluetooth.zhjbluetoothsdk.bean.HealthSleepItem
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException
import java.util.*


class SleepActivity : BaseActivity(), OnChartValueSelectedListener {
    private var dateIndex = 0
    private var dailyDateIndex = 0
    private var dateType = DateType.Days
    private var currentMonth = Date()
    private var needSyncTrend = true
    private var todayCalendar = Calendar.getInstance()
    private var trendCalendar = Calendar.getInstance()
    private var healthSleepItems = mutableListOf<List<HealthSleepItem>?>()
    private var trendSleepItems = mutableListOf<List<HealthSleepItem>?>()

    private var daiySleepValues = mutableListOf<SleepModel>()
    private var trendSleepValues = mutableListOf<SleepModel>()

    override fun initData() {
        findViewById<TextView>(R.id.tv_time).text = DateUtil.getYMDate(Date())
        findViewById<View>(R.id.v_date_type).setOnClickListener {
            XPopup.Builder(this).atView(findViewById<View>(R.id.v_date_type)).asAttachList(
                arrayOf("Days", "Weeks", "Months"),
                null
            ) { _, text ->
                if (dateType == DateType.valueOf(text)) return@asAttachList
                dateType = DateType.valueOf(text)
                findViewById<TextView>(R.id.iv_date_type).text = text
                dateIndex = 0
                trendCalendar = Calendar.getInstance()
                trendSleepItems.clear()
                syncTrendSleepHistory()
            }.show()
        }
        findViewById<ScrollDateView>(R.id.rl_scroll).selectCallBack = {  date : Date ->
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
                findViewById<ScrollDateView>(R.id.rl_scroll).updateMonthUI(currentMonth)

                val days = DateUtil.computeMonthDayCount(year, monthOfYear)
                val thisCalendar = Calendar.getInstance()
                thisCalendar!!.time = Date()
                var selectDay = days / 2
                if (thisCalendar!!.get(Calendar.YEAR) == year && thisCalendar!!.get(Calendar.MONTH) == monthOfYear) {
                    selectDay = calendar!!.get(Calendar.DATE)
                }
                calendar!!.set(Calendar.DATE, selectDay)
                selectDate(calendar.time)
            }

            dialogFragment.show(supportFragmentManager, null)
        }

        setupPieChart(findViewById<PieChart>(R.id.pc_daily_chart))
        setupTrendChart(findViewById<LineChart>(R.id.lc_trend_chart))

        healthSleepItems.clear()
        syncDailySleepHistory()
    }

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
                                healthSleepItems.add(sleepItems!!)
                            }
                            if (dailyDateIndex >= 2) {
                                drawDailyChart()
                                if (needSyncTrend) {
                                    syncTrendSleepHistory()
                                }
                                return
                            }
                            todayCalendar.add(Calendar.DATE, -1)
                            syncDailySleepHistory()
                        } else {
                            drawDailyChart()
                            if (needSyncTrend) {
                                syncTrendSleepHistory()
                            }
                        }
                    }
                }

                override fun onFailed(e: WriteBleException) {
                    if (needSyncTrend) {
                        syncTrendSleepHistory()
                    }
                }
            })
    }

    private fun syncTrendSleepHistory() {
        dateIndex++
        val year: Int = trendCalendar.get(Calendar.YEAR)
        val month: Int = trendCalendar.get(Calendar.MONTH) + 1
        val day: Int = trendCalendar.get(Calendar.DATE)
        BleSdkWrapper.getStepOrSleepHistory(
            year,
            month,
            day,
            object : OnLeWriteCharacteristicListener() {
                override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                    if (handlerBleDataResult.isComplete) {
                        if (handlerBleDataResult.hasNext) { //是否还有更多的历史数据
                            val sleepItems =
                                handlerBleDataResult.sleepItems
                            trendSleepItems.add(sleepItems)
                            when(dateType) {
                                DateType.Days-> {
                                    if (dateIndex >= 7) {
                                        return drawTrendChart()
                                    }
                                }
                                DateType.Weeks -> {
                                    if (dateIndex >= 28) {
                                        return drawTrendChart()
                                    }
                                }

                                DateType.Months -> {
                                    if (dateIndex >= 90) {
                                        return drawTrendChart()
                                    }
                                }

                            }
                            trendCalendar.add(Calendar.DATE, -1)
                            syncTrendSleepHistory()
                        } else {
                            drawTrendChart()
                        }
                    }
                }

                override fun onFailed(e: WriteBleException) {}
            })
    }


    private fun computeMath() :Triple<Int, Int, Int>?{
        var deep = 0
        var light = 0
        var wide: Int = 0
        if (healthSleepItems.isEmpty()) {
            return Triple(0, 0, 0)
        }
        val sleepItems = healthSleepItems.first()

        for(item in sleepItems!!) {
            if (item!!.sleepStatus == 2) {
                light += 10
            }
            else if (item!!.sleepStatus == 3) {
                deep += 10
            }
            else if (item!!.sleepStatus == 4) {
                wide += 10
            }
        }
        return Triple(deep, light, wide)

    }

    private fun drawLatest(total:Int) {
        var hour = total / 60
        var minu = total % 60
        findViewById<TextView>(R.id.tv_duration_hr).text = String.format("%02d", hour)
        findViewById<TextView>(R.id.tv_duration_min).text = String.format("%02d", minu)
    }


    private fun drawTrendChart() {
        if (trendSleepItems.isEmpty())return
        var trendItems = mutableListOf<Int>()
        for (items in trendSleepItems) {
            var average: Int = 0
            var valid = 0
            var sum = 0
            if (items == null || items.isEmpty()) {
                trendItems.add(0)
                break
            }
            for(item in items!!) {
                if (item.sleepStatus == 2 || item.sleepStatus == 3 || item.sleepStatus == 4) {
                    sum += 10
                }
            }
            trendItems.add(sum)
        }
        var total = when(dateType) {
            DateType.Days-> 7
            DateType.Weeks -> 28
            DateType.Months -> 90
        }
        var trendValues = mutableListOf<Int>()
        var reversedTrends = trendItems.reversed()
        if (trendItems.count() < total) {
            val empty = total - trendItems.count()
            for (i in 0 until empty) {
                trendValues.add(0)
            }
        }
        for (item in reversedTrends) {
            trendValues.add(item)
        }
        setupTrendData(trendValues)
        drawTrendAxis()
    }

    private fun makeAttributeString(minute: Int):Spannable {
        var hour = minute / 60
        var minu = minute % 60
        if (hour == 0) {
            val minText = "${minu}min"
            var minString: Spannable = SpannableString(minText)
            minString.setSpan(StyleSpan(Typeface.BOLD), 0, minText.length - 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            minString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.dark)), 0, minText.length - 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            minString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)), 0, minText.length - 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            minString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)), minText.length - 3, minText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            minString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)), minText.length - 3, minText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            return minString
        } else if(minu == 0) {
            val minText = "${hour}hr"
            var minString: Spannable = SpannableString(minText)
            minString.setSpan(StyleSpan(Typeface.BOLD), 0, minText.length - 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            minString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.dark)), 0, minText.length - 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            minString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)), 0, minText.length - 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            minString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)), minText.length - 2, minText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            minString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)), minText.length - 2, minText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            return minString
        } else {
            val minText = "${minu}min"
            var minString: Spannable = SpannableString(minText)
            minString.setSpan(StyleSpan(Typeface.BOLD), 0, minText.length - 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            minString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.dark)), 0, minText.length - 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            minString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)), 0, minText.length - 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            minString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)), minText.length - 3, minText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            minString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)), minText.length - 3, minText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            val hourText = "${hour}hr "
            var hourString: Spannable = SpannableString(hourText)
            hourString.setSpan(StyleSpan(Typeface.BOLD), 0, hourText.length - 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            hourString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.dark)), 0, hourText.length - 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            hourString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)), 0, hourText.length - 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            hourString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)), hourText.length - 3, hourText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            hourString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)), hourText.length - 3, hourText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            val summary = SpannableStringBuilder()
            summary.append(hourString)
            summary.append(minString)
            return summary
        }
    }

    private fun drawDailyChart() {
        if (healthSleepItems.isEmpty()) return
        val item = computeMath() ?: return
        val (deep, light, wide) = item
        findViewById<TextView>(R.id.tv_deepsleep_value).text = makeAttributeString(deep)
        findViewById<TextView>(R.id.tv_light_value).text = makeAttributeString(light)
        findViewById<TextView>(R.id.tv_awake_value).text = makeAttributeString(wide)

        drawLatest(deep + light + wide)
        setupPieData()
        drawDailyAxis()
    }

    private fun drawDailyAxis() {
//        val linearLayout = findViewById<LinearLayout>(R.id.ll_hr_axis)
//        linearLayout.removeAllViews()
//        for (i in 0..4) {
//            val textView = TextView(linearLayout.context)
//            val layoutParams = LinearLayout.LayoutParams(
//                0,
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                1.0f
//            )
//            textView.layoutParams = layoutParams
//            textView.text = String.format("%02d:00", i * 6)
//            textView.textSize = 10f
//            textView.setTextColor(ColorUtils.getColor(R.color.light_gary))
//            textView.gravity = Gravity.CENTER
//            linearLayout.addView(textView)
//        }
    }

    private fun drawTrendAxis() {
        val linearLayout = findViewById<LinearLayout>(R.id.ll_trend_axis)
        linearLayout.removeAllViews()
        var mulity = 1
        var total = 7
        when(dateType) {
            DateType.Days-> {
                total = 7
                mulity = 1
            }
            DateType.Weeks -> {
                total = 4
                mulity = 7
            }
            DateType.Months -> {
                total = 3
                mulity = 30
            }
        }

        for (i in 0 until total) {
            val current = total - i - 1
            val textView = TextView(linearLayout.context)
            val layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0f
            )
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, -1 * current * mulity)
            textView.layoutParams = layoutParams
            textView.text = DateUtil.getYMDDate(calendar.time)
            textView.textSize = 10f
            textView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            // 参数：int autoSizeMinTextSize, int autoSizeMaxTextSize, int autoSizeStepGranularity, int unit
            textView.setAutoSizeTextTypeUniformWithConfiguration(5, 10, 1, TypedValue.COMPLEX_UNIT_SP)
            textView.setTextColor(ColorUtils.getColor(R.color.light_gary))
            textView.gravity = Gravity.CENTER
            linearLayout.addView(textView)
        }
    }


    private fun selectDate(date: Date) {
        dailyDateIndex = 0
        todayCalendar.time = date
        needSyncTrend = false
        healthSleepItems.clear()
        syncDailySleepHistory()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_sleep
    }

    override fun onListener() {
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }

    private fun setupPieChart(chart: PieChart) {
        chart.setNoDataText("")
        chart.holeRadius = 80f
        chart.transparentCircleRadius = 80f
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        chart.setDrawCenterText(false)
        chart.setDrawEntryLabels(false)
        chart.setEntryLabelColor(Color.WHITE)
        chart.setTouchEnabled(false)
        chart.setDrawRoundedSlices(true)
    }

    private fun setupTrendChart(chart: LineChart) {
        // background color
        chart.setBackgroundColor(Color.WHITE)
        chart.setNoDataText(resources.getString(R.string.no_sleep_data))
        // disable description text
        chart.description.isEnabled = false

        // enable touch gestures
        chart.setTouchEnabled(true)

        // set listeners
        chart.setOnChartValueSelectedListener(this)
        chart.setDrawGridBackground(false)

        // create marker to display box when values are selected
        val mv = MyMarkerView(this, R.layout.custom_marker_view)

        // Set the marker to the chart
        mv.chartView = chart
        chart.marker = mv

        // enable scaling and dragging
        chart.setDragEnabled(true)
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)

        var xAxis: XAxis
        run {
            // // X-Axis Style // //
            xAxis = chart!!.xAxis
            xAxis.setDrawLabels(false)
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(false)
            xAxis.setDrawLabels(false)
            xAxis.xOffset = 0f
            xAxis.yOffset = 0f
            xAxis.axisLineColor = ColorUtils.getColor(R.color.light_gary2)
            xAxis.textColor = ColorUtils.getColor(R.color.light_gary)

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f)
        }
        var yAxis: YAxis
        run {
            // // Y-Axis Style // //
            yAxis = chart!!.axisLeft

            // disable dual axis (only use LEFT axis)
            chart!!.axisRight.isEnabled = false
            chart.axisRight.setDrawLabels(false)
            chart.axisRight.setDrawAxisLine(false)
            yAxis.setDrawLabels(true)
            yAxis.setDrawAxisLine(false)
            yAxis.axisLineColor = ColorUtils.getColor(R.color.light_gary)
            yAxis.textColor = ColorUtils.getColor(R.color.light_gary)
        }
        yAxis.setDrawLimitLinesBehindData(false)
        xAxis.setDrawLimitLinesBehindData(false)

        chart.setNoDataText(resources.getString(R.string.no_chart_data))

        // draw points over time
        chart!!.animateX(1500)
        chart.legend.isEnabled = false
    }

    private fun setupPieData() {
        val chart = findViewById<PieChart>(R.id.pc_daily_chart)
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(0.5F))
        entries.add(PieEntry(0.7F))
        entries.add(PieEntry(1.5F))

        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawValues(false)

        dataSet.setDrawIcons(false)

        dataSet.selectionShift = 0f
        val colors = ArrayList<Int>()
        colors.add(ColorUtils.getColor(R.color.purple_700))
        colors.add(ColorUtils.getColor(R.color.purple_200))
        colors.add(ColorUtils.getColor(R.color.orange))

        dataSet.colors = colors
        dataSet.selectionShift = 0f;
        val data = PieData(dataSet)
        chart.data = data
        chart.highlightValues(null)
        chart.invalidate()
    }

    private fun setupTrendData(trendItems: List<Int>) {
        val chart = findViewById<LineChart>(R.id.lc_trend_chart)
        val values = ArrayList<Entry>()
        for (index in trendItems.indices) {
            val item = trendItems[index]
            val x = (index + 1.0) / trendItems.count()
            values.add(Entry(x.toFloat(), item.toFloat()))
        }
        val set1 = LineDataSet(values,"")
        set1.setDrawIcons(false)
        set1.setDrawCircleHole(false)
        set1.setDrawCircles(false)
        set1.setDrawValues(false)
//        set1.
        set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        // line thickness and point size
        set1.lineWidth = 1f

        // draw points as solid circles
        set1.setDrawCircleHole(false)
        set1.color = ColorUtils.getColor(R.color.primary_blue)

        // text size of values
        set1.valueTextSize = 9f

        // set the filled area
        set1.setDrawFilled(true)
        set1.fillFormatter =
            IFillFormatter { dataSet, dataProvider -> chart!!.axisLeft.axisMinimum }
        // set color of filled area
        if (Utils.getSDKInt() >= 18) {
            // drawables only supported on api level 18 and above
            val drawable = ContextCompat.getDrawable(this, R.drawable.fade_daily_hr)
            set1.fillDrawable = drawable
        } else {
            set1.fillColor = Color.WHITE
        }
        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(set1) // add the data sets

        // create a data object with the data sets
        val data = LineData(dataSets)

        // set data
        chart.data = data
        chart!!.animateX(1500)
    }
}