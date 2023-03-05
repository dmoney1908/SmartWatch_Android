package com.linhua.smartwatch.heartrate

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ColorUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
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
import com.zhj.bluetooth.zhjbluetoothsdk.bean.HealthHeartRateItem
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException
import java.util.*


class HeartRateActivity : BaseActivity(), OnChartValueSelectedListener {
    private var dateType = DateType.Days
    private var currentMonth = Date()
    private var todayCalendar = Calendar.getInstance()
    private var healthHeartRateItemsAll = mutableListOf<HealthHeartRateItem?>()
    private var trendHeartRateItems = mutableListOf<HealthHeartRateItem?>()

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
        setupChart()
        syncDailyHeartHistory()
    }

    private fun syncDailyHeartHistory() {
        healthHeartRateItemsAll.clear()
        val year: Int = todayCalendar.get(Calendar.YEAR)
        val month: Int = todayCalendar.get(Calendar.MONTH) + 1
        val day: Int = todayCalendar.get(Calendar.DATE)
        BleSdkWrapper.getHistoryHeartRateData(
            year,
            month,
            day,
            object : OnLeWriteCharacteristicListener() {
                override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                    if (handlerBleDataResult.isComplete) {
                        val healthHeartRateItems =
                            handlerBleDataResult.data as List<HealthHeartRateItem>
                        healthHeartRateItemsAll.addAll(healthHeartRateItems)
                        drawDailyChart()
                        syncTrendHeartHistory()
                    }
                }

                override fun onFailed(e: WriteBleException) {
                    syncTrendHeartHistory()
                }
            })
    }

    private var dateIndex = 0

    private fun syncTrendHeartHistory() {
        dateIndex++
        val year: Int = todayCalendar.get(Calendar.YEAR)
        val month: Int = todayCalendar.get(Calendar.MONTH) + 1
        val day: Int = todayCalendar.get(Calendar.DATE)
        BleSdkWrapper.getHistoryHeartRateData(
            year,
            month,
            day,
            object : OnLeWriteCharacteristicListener() {
                override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                    if (handlerBleDataResult.isComplete) {
                        if (handlerBleDataResult.hasNext) { //是否还有更多的历史数据
                            val healthHeartRateItems =
                                handlerBleDataResult.data as List<HealthHeartRateItem>
                            healthHeartRateItemsAll.addAll(healthHeartRateItems)
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
                            todayCalendar.add(Calendar.DATE, -1)
                            syncTrendHeartHistory()
                        } else {
                            drawTrendChart()
                        }
                    }
                }

                override fun onFailed(e: WriteBleException) {}
            })
    }


    private fun computeMath() :Triple<Int, Int, Int>{
        var min = 0
        var max = 0
        var average: Int = 0
        var sum = 0
        var valid = 0
        if (healthHeartRateItemsAll.isEmpty()) {
            return Triple(min, max, average)
        }
        for(item in healthHeartRateItemsAll) {
            if (item!!.heartRaveValue > max) {
                max = item.heartRaveValue
            }
            if (item.heartRaveValue < min && item.heartRaveValue > 10) {
                min = item.heartRaveValue
            }
            if (item.heartRaveValue > 10) {
                sum += item.heartRaveValue
                valid++
            }

        }
        if (valid > 0) {
            average = sum / valid
        }
        return Triple(min, max, average)
    }

    private fun drawTrendChart() {


    }

    private fun drawDailyChart() {
        if (healthHeartRateItemsAll.isEmpty()) return
        val (min, max, average) = computeMath()

        val minText = "$min Bpm"
        var minString: Spannable = SpannableString(minText)
        minString.setSpan(StyleSpan(Typeface.BOLD), 0, minText.length - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        minString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.dark)), 0, minText.length - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        minString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)), 0, minText.length - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        minString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)), minText.length - 4, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        minString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)), minText.length - 4, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        findViewById<TextView>(R.id.tv_lowest_value).text = minString

        val maxText = "$max Bpm"
        var maxString: Spannable = SpannableString(maxText)
        maxString.setSpan(StyleSpan(Typeface.BOLD), 0, maxText.length - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        maxString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.dark)), 0, maxText.length - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        maxString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)), 0, maxText.length - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        maxString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)), maxText.length - 4, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        maxString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)), maxText.length - 4, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        findViewById<TextView>(R.id.tv_highest_value).text = maxString

        val averageText = "$average Bpm"
        var averageString: Spannable = SpannableString(averageText)
        averageString.setSpan(StyleSpan(Typeface.BOLD), 0, averageText.length - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        averageString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.dark)), 0, averageText.length - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        averageString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)), 0, averageText.length - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        averageString.setSpan(AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)), averageText.length - 4, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        averageString.setSpan(ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)), averageText.length - 4, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        findViewById<TextView>(R.id.tv_average_value).text = averageString
        val item = healthHeartRateItemsAll.last()
        findViewById<TextView>(R.id.tv_last_time).text = String.format("%02d:%02d", item!!.hour, item!!.minuter)
        findViewById<TextView>(R.id.tv_hr).text = item!!.heartRaveValue.toString()
        setupDailyData()
    }


    private fun selectDate(date: Date) {

    }

    override fun getLayoutId(): Int {
        return R.layout.activity_heart_rate
    }

    override fun onListener() {
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }

    private fun setupChart() {
        // // Chart Style // //
        val chart = findViewById<LineChart>(R.id.lc_daily_chart)

        // background color
        chart.setBackgroundColor(Color.WHITE)

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

    private fun setupDailyData() {
        val chart = findViewById<LineChart>(R.id.lc_daily_chart)
        val values = ArrayList<Entry>()
        for (index in healthHeartRateItemsAll.indices) {
            val item = healthHeartRateItemsAll[index]
            val x = (index + 1.0) / healthHeartRateItemsAll.count()
            val value = item!!.heartRaveValue
            values.add(Entry(x.toFloat(), value.toFloat()))
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
    }
}