package com.linhua.smartwatch.tempr

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.Utils
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.CommonActivity
import com.linhua.smartwatch.chart.MyMarkerView
import com.linhua.smartwatch.databinding.ActivityTemperatureBinding
import com.linhua.smartwatch.helper.UserData
import com.linhua.smartwatch.monthpicker.MonthYearPickerDialogFragment
import com.linhua.smartwatch.utils.CommonUtil.AutoTempr
import com.linhua.smartwatch.utils.CommonUtil.FahFromCelTempr
import com.linhua.smartwatch.utils.DateType
import com.linhua.smartwatch.utils.DateUtil
import com.linhua.smartwatch.utils.DeviceManager
import com.linhua.smartwatch.view.ScrollDateView
import com.lxj.xpopup.XPopup
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.zhj.bluetooth.zhjbluetoothsdk.bean.TempInfo
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException
import java.util.*

class TemperatureActivity : CommonActivity(), OnChartValueSelectedListener {
    private var dateType = DateType.Days
    private var currentMonth = Date()
    private var needSyncTrend = true
    private var todayCalendar = Calendar.getInstance()
    private var trendCalendar = Calendar.getInstance()
    private var temprItemsAll = mutableListOf<TempInfo>()
    private var trendTemprItems = mutableListOf<List<TempInfo>>()
    private var refreshLayout: RefreshLayout? = null

    private lateinit var binding: ActivityTemperatureBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTemperatureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvTime.text = DateUtil.getYMDate(Date())
        binding.vDateType.setOnClickListener {
            XPopup.Builder(this).atView(findViewById<View>(R.id.v_date_type)).asAttachList(
                arrayOf("Days", "Weeks", "Months"), null
            ) { _, text ->
                if (dateType == DateType.valueOf(text)) return@asAttachList
                dateType = DateType.valueOf(text)
                findViewById<TextView>(R.id.iv_date_type).text = text
                dateIndex = 0
                trendCalendar = Calendar.getInstance()
                trendTemprItems.clear()
                syncTrendHeartHistory()
            }.show()
        }
        refreshLayout = binding.refreshLayout as RefreshLayout
        refreshLayout!!.setRefreshHeader(ClassicsHeader(this))

        refreshLayout!!.apply {
            setOnRefreshListener {
                currentMonth = Date()
                findViewById<ScrollDateView>(R.id.rl_scroll).updateMonthUI(currentMonth)
                findViewById<TextView>(R.id.tv_time).text = DateUtil.getYMDate(currentMonth)
                selectDate(currentMonth)
            }
        }
        binding.baseTitleBack.setOnClickListener {
            onBackPressed()
        }
        binding.rlScroll.selectCallBack = { date: Date ->
            selectDate(date)
        }
        binding.rlMonth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val todayYear = calendar[Calendar.YEAR]
            val todayMonth = calendar[Calendar.MONTH]
            calendar.time = currentMonth
            val yearSelected = calendar[Calendar.YEAR]
            val monthSelected = calendar[Calendar.MONTH]
            MonthYearPickerDialogFragment.getInstance(monthSelected, yearSelected)
            calendar.clear()
            calendar.set(2022, 1, 1)
            val minDate = calendar.timeInMillis // Get milliseconds of the modified date
            calendar.clear()
            calendar.set(todayYear, todayMonth, 1)
            val maxDate = calendar.timeInMillis
            val dialogFragment = MonthYearPickerDialogFragment.getInstance(
                monthSelected, yearSelected, minDate, maxDate
            )
            dialogFragment.setOnDateSetListener { year, monthOfYear ->
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                findViewById<TextView>(R.id.tv_time).text = DateUtil.getYMDate(calendar.time)
                currentMonth = calendar.time
                findViewById<ScrollDateView>(R.id.rl_scroll).updateMonthUI(currentMonth)

                val days = DateUtil.computeMonthDayCount(year, monthOfYear)
                val thisCalendar = Calendar.getInstance()
                thisCalendar.time = Date()
                var selectDay = days / 2
                if (thisCalendar.get(Calendar.YEAR) == year && thisCalendar.get(Calendar.MONTH) == monthOfYear) {
                    selectDay = calendar.get(Calendar.DATE)
                }
                calendar.set(Calendar.DATE, selectDay)
                selectDate(calendar.time)
            }

            dialogFragment.show(supportFragmentManager, null)
        }
        setupChart(binding.bcDailyChart)
        setupTrendChart(binding.lcTrendChart)
        syncDailyTemprHistory()
    }

    private fun syncDailyTemprHistory() {
        if (!DeviceManager.isSDKAvailable || DeviceManager.getConnectedDevice() == null) {
            refreshLayout!!.finishRefresh(false)
        }
        temprItemsAll.clear()
        val year: Int = todayCalendar.get(Calendar.YEAR)
        val month: Int = todayCalendar.get(Calendar.MONTH) + 1
        val day: Int = todayCalendar.get(Calendar.DATE)

        BleSdkWrapper.getHistoryTemp(year,
            month,
            day,
            object : OnLeWriteCharacteristicListener() {
                override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                    if (handlerBleDataResult.isComplete) {
                        val tempInfos =
                            handlerBleDataResult.data as List<TempInfo>?
                        if (tempInfos != null) {
                            temprItemsAll.addAll(tempInfos)
                        }
                        drawDailyChart()
                        if (needSyncTrend) {
                            syncTrendHeartHistory()
                        }
                        refreshLayout!!.finishRefresh(true)
                    }
                }

                override fun onFailed(e: WriteBleException) {
                    if (needSyncTrend) {
                        syncTrendHeartHistory()
                    }
                    refreshLayout!!.finishRefresh(false)
                }
            })
    }

    private var dateIndex = 0

    private fun syncTrendHeartHistory() {
        dateIndex++
        val year: Int = trendCalendar.get(Calendar.YEAR)
        val month: Int = trendCalendar.get(Calendar.MONTH) + 1
        val day: Int = trendCalendar.get(Calendar.DATE)
        BleSdkWrapper.getHistoryTemp(year,
            month,
            day,
            object : OnLeWriteCharacteristicListener() {
                override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                    if (handlerBleDataResult.isComplete) {
                        if (handlerBleDataResult.hasNext) { //是否还有更多的历史数据
                            val healthHeartRateItems =
                                handlerBleDataResult.data as List<TempInfo>
                            trendTemprItems.add(healthHeartRateItems)
                            when (dateType) {
                                DateType.Days -> {
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
                            syncTrendHeartHistory()
                        } else {
                            drawTrendChart()
                        }
                    }
                }

                override fun onFailed(e: WriteBleException) {}
            })
    }

    private fun computeMath(): Float? {
        var sum = 0.0f
        var valid = 0
        if (temprItemsAll.isEmpty()) {
            return null
        }
        for (item in temprItemsAll) {
            if (item.tmpHandler > 10) {
                sum += item.tmpHandler
                valid++
            }
        }
        return if (valid > 0) {
            sum / valid
        } else {
            null
        }
    }

    private fun drawLatest() {
        for (item in temprItemsAll.reversed()) {
            if (item.tmpHandler > 10) {
                binding.tvLastTime.text =
                    String.format("%02d:%02d", item.hour, item.minute)
                binding.tvTempr.text =  String.format("%.1f", AutoTempr(item.tmpHandler.toFloat()) / 100f)
                return
            }
        }
    }


    private fun drawTrendChart() {
        if (trendTemprItems.isEmpty()) return
        val trendItems = mutableListOf<Int>()
        for (items in trendTemprItems) {
            var average: Int = 0
            var valid = 0
            var sum = 0
            if (items == null || items.isEmpty()) {
                trendItems.add(0)
                break
            }
            for (item in items) {
                if (item.tmpHandler > 10) {
                    sum += item.tmpHandler
                    valid++
                }
            }
            if (valid > 0) {
                average = sum / valid
                trendItems.add(average)
            } else {
                trendItems.add(0)
            }
        }
        val total = when (dateType) {
            DateType.Days -> 7
            DateType.Weeks -> 28
            DateType.Months -> 90
        }
        val trendValues = mutableListOf<Int>()
        val reversedTrends = trendItems.reversed()
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

    private fun drawDailyChart() {
        if (temprItemsAll.isEmpty()) return
        val averageValue = computeMath() ?: return
        var min = ""
        var max = ""
        var unit = StringUtils.getString(R.string.tempr_f)
        if (UserData.systemSetting.temprUnit == 0) {
            unit = StringUtils.getString(R.string.tempr_f)
            min = String.format("<%.1f", FahFromCelTempr(36.0F))
            max = String.format(">%.1f", FahFromCelTempr( 37.3F))
        } else {
            unit = StringUtils.getString(R.string.tempr_c)
            min = "<36.0"
            max = ">37.3"
        }
        var average = String.format("%.1f", AutoTempr(averageValue / 100.0F))

        val minText = "$min $unit"
        val minString: Spannable = SpannableString(minText)
        minString.setSpan(
            StyleSpan(Typeface.BOLD), 0, minText.length - 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        minString.setSpan(
            ForegroundColorSpan(ColorUtils.getColor(R.color.dark)),
            0,
            minText.length - 3,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        minString.setSpan(
            AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)),
            0,
            minText.length - 3,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        minString.setSpan(
            AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)),
            minText.length - 3,
            minText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        minString.setSpan(
            ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)),
            minText.length - 3,
            minText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        findViewById<TextView>(R.id.tv_lowest_value).text = minString

        val maxText = "$max $unit"
        val maxString: Spannable = SpannableString(maxText)
        maxString.setSpan(
            StyleSpan(Typeface.BOLD), 0, maxText.length - 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        maxString.setSpan(
            ForegroundColorSpan(ColorUtils.getColor(R.color.dark)),
            0,
            maxText.length - 3,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        maxString.setSpan(
            AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)),
            0,
            maxText.length - 3,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        maxString.setSpan(
            AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)),
            maxText.length - 3,
            maxText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        maxString.setSpan(
            ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)),
            maxText.length - 3,
            maxText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        findViewById<TextView>(R.id.tv_highest_value).text = maxString

        val averageText = "$average $unit"
        val averageString: Spannable = SpannableString(averageText)
        averageString.setSpan(
            StyleSpan(Typeface.BOLD), 0, averageText.length - 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        averageString.setSpan(
            ForegroundColorSpan(ColorUtils.getColor(R.color.dark)),
            0,
            averageText.length - 3,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        averageString.setSpan(
            AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)),
            0,
            averageText.length - 3,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        averageString.setSpan(
            AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)),
            averageText.length - 3,
            averageText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        averageString.setSpan(
            ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)),
            averageText.length - 3,
            averageText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        findViewById<TextView>(R.id.tv_average_value).text = averageString

        drawLatest()
        setupDailyData()
        drawDailyAxis()
    }

    private fun drawDailyAxis() {
        val linearLayout = findViewById<LinearLayout>(R.id.ll_hr_axis)
        linearLayout.removeAllViews()
        for (i in 0..4) {
            val textView = TextView(linearLayout.context)
            val layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f
            )
            textView.layoutParams = layoutParams
            textView.text = DateUtil.convert24To12Hour(i * 6)
            textView.textSize = 10f
            textView.setTextColor(ColorUtils.getColor(R.color.light_gary))
            textView.gravity = Gravity.CENTER
            linearLayout.addView(textView)
        }
    }

    private fun drawTrendAxis() {
        val linearLayout = findViewById<LinearLayout>(R.id.ll_trend_axis)
        linearLayout.removeAllViews()
        var mulity = 1
        var total = 7
        when (dateType) {
            DateType.Days -> {
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
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f
            )
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, -1 * current * mulity)
            textView.layoutParams = layoutParams
            textView.text = DateUtil.getYMDDate(calendar.time)
            textView.textSize = 10f
            textView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
            // 参数：int autoSizeMinTextSize, int autoSizeMaxTextSize, int autoSizeStepGranularity, int unit
            textView.setAutoSizeTextTypeUniformWithConfiguration(
                5, 10, 1, TypedValue.COMPLEX_UNIT_SP
            )
            textView.setTextColor(ColorUtils.getColor(R.color.light_gary))
            textView.gravity = Gravity.CENTER
            linearLayout.addView(textView)
        }
    }


    private fun selectDate(date: Date) {
        todayCalendar.time = date
        needSyncTrend = false
        temprItemsAll.clear()
        syncDailyTemprHistory()
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }

    private fun setupChart(chart: BarChart) {
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
            xAxis = chart.xAxis
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
            yAxis = chart.axisLeft

            // disable dual axis (only use LEFT axis)
            chart.axisRight.isEnabled = false
            chart.axisRight.setDrawLabels(false)
            chart.axisRight.setDrawAxisLine(false)
            yAxis.setDrawLabels(true)
            yAxis.setDrawAxisLine(false)
            yAxis.axisLineColor = ColorUtils.getColor(R.color.light_gary)
            yAxis.textColor = ColorUtils.getColor(R.color.light_gary)
        }
        yAxis.setDrawLimitLinesBehindData(false)
        xAxis.setDrawLimitLinesBehindData(false)

        chart.setNoDataText(resources.getString(R.string.no_oxygen_data))

        // draw points over time
        chart.animateX(1500)
        chart.legend.isEnabled = false
    }

    private fun setupTrendChart(chart: LineChart) {
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
            xAxis = chart.xAxis
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
            yAxis = chart.axisLeft

            // disable dual axis (only use LEFT axis)
            chart.axisRight.isEnabled = false
            chart.axisRight.setDrawLabels(false)
            chart.axisRight.setDrawAxisLine(false)
            yAxis.setDrawLabels(true)
            yAxis.setDrawAxisLine(false)
            yAxis.axisLineColor = ColorUtils.getColor(R.color.light_gary)
            yAxis.textColor = ColorUtils.getColor(R.color.light_gary)
        }
        yAxis.setDrawLimitLinesBehindData(false)
        xAxis.setDrawLimitLinesBehindData(false)

        chart.setNoDataText(resources.getString(R.string.no_tempr_data))

        // draw points over time
        chart.animateX(1500)
        chart.legend.isEnabled = false
    }

    private fun setupDailyData() {
        val chart = binding.bcDailyChart
        val values = ArrayList<BarEntry>()
        for (index in temprItemsAll.indices) {
            val item = temprItemsAll[index]
            val x = index + 1.0
            val value = AutoTempr(item.tmpHandler / 100F)
            values.add(BarEntry(x.toFloat(), value.toFloat()))
        }
        val set1 = BarDataSet(values, "")
        set1.setDrawIcons(false)
        set1.setDrawValues(false)
        set1.color = ColorUtils.getColor(R.color.primary_blue)

        // text size of values
        set1.valueTextSize = 9f
        // set color of filled area
        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(set1) // add the data sets

        // create a data object with the data sets
        val data = BarData(dataSets)

        // set data
        chart.data = data
        data.barWidth = 0.7f
        chart.animateX(1500)
    }

    private fun setupTrendData(trendItems: List<Int>) {
        val chart = findViewById<LineChart>(R.id.lc_trend_chart)
        val values = ArrayList<Entry>()
        for (index in trendItems.indices) {
            val item = trendItems[index]
            val x = (index + 1.0) / trendItems.count()
            val value: Float = AutoTempr(item.toFloat() / 100.0F).toFloat()
            values.add(Entry(x.toFloat(), value))
        }
        val set1 = LineDataSet(values, "")
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
