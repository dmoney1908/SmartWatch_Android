package com.linhua.smartwatch.bp

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.linhua.smartwatch.utils.DeviceManager
import com.linhua.smartwatch.view.ScrollDateView
import com.lxj.xpopup.XPopup
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.zhj.bluetooth.zhjbluetoothsdk.bean.HealthHeartRateItem
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException
import java.util.*

class BPActivity : BaseActivity(), OnChartValueSelectedListener {
    private var dateType = DateType.Days
    private var currentMonth = Date()
    private var needSyncTrend = true
    private var todayCalendar = Calendar.getInstance()
    private var trendCalendar = Calendar.getInstance()
    private var healthHeartRateItemsAll = mutableListOf<HealthHeartRateItem?>()
    private var trendHeartRateItems = mutableListOf<List<HealthHeartRateItem>?>()
    private var refreshLayout: RefreshLayout? = null

    override fun initData() {
        findViewById<TextView>(R.id.tv_time).text = DateUtil.getYMDate(Date())
        findViewById<ImageView>(R.id.base_title_back).setOnClickListener {
            onBackPressed()
        }
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
                trendHeartRateItems.clear()
                syncTrendHeartHistory()
            }.show()
        }
        findViewById<ScrollDateView>(R.id.rl_scroll).selectCallBack = { date: Date ->
            selectDate(date)
        }
        refreshLayout = findViewById(R.id.refreshLayout) as RefreshLayout
        refreshLayout!!.setRefreshHeader(ClassicsHeader(this))

        refreshLayout!!.apply {
            setOnRefreshListener {
                currentMonth = Date()
                findViewById<ScrollDateView>(R.id.rl_scroll).updateMonthUI(currentMonth)
                findViewById<TextView>(R.id.tv_time).text = DateUtil.getYMDate(currentMonth)
                selectDate(currentMonth)
            }
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
            calendar.set(2022, 1, 1)
            val minDate = calendar.timeInMillis // Get milliseconds of the modified date
            calendar.clear()
            calendar.set(todayYear, todayMonth, 1)
            val maxDate = calendar.timeInMillis
            val dialogFragment = MonthYearPickerDialogFragment.getInstance(
                monthSelected,
                yearSelected,
                minDate,
                maxDate
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
        setupChart(findViewById<LineChart>(R.id.lc_daily_chart))
        setupChart(findViewById<LineChart>(R.id.lc_trend_chart))
        syncDailyHeartHistory()
    }

    private fun syncDailyHeartHistory() {
        if (!DeviceManager.isSDKAvailable || DeviceManager.getConnectedDevice() == null) {
            refreshLayout!!.finishRefresh(false)
        }
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
                            handlerBleDataResult.data as List<HealthHeartRateItem>?
                        if (healthHeartRateItems != null) {
                            healthHeartRateItemsAll.addAll(healthHeartRateItems)
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
                            trendHeartRateItems.add(healthHeartRateItems)
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


    private fun computeMath(): BPMathModel? {
        var minFZ = 300
        var maxSS = 0
        var averageSS = 0
        var averageFZ = 0
        var sumSS = 0
        var validSS = 0
        var sumFZ = 0
        var validFZ = 0
        if (healthHeartRateItemsAll.isEmpty()) {
            return null
        }
        for (item in healthHeartRateItemsAll) {
            if (item!!.ss > maxSS) {
                maxSS = item.ss
            }
            if (item.heartRaveValue in 11 until minFZ) {
                minFZ = item.fz
            }
            if (item.ss > 10) {
                sumSS += item.ss
                validSS++
            }
            if (item.fz > 10) {
                sumFZ += item.fz
                validFZ++
            }
        }
        return if (validSS > 0 && validFZ > 0) {
            averageSS = sumSS / validSS
            averageFZ = sumFZ / validFZ
            BPMathModel(maxSS, minFZ, averageSS, averageFZ)
        } else {
            null
        }
    }

    private fun drawLatest() {
        for (item in healthHeartRateItemsAll.reversed()) {
            if (item!!.fz > 10 && item.ss > 10) {
                findViewById<TextView>(R.id.tv_last_time).text = DateUtil.convert24To12Time(item.hour, item.minuter)
                findViewById<TextView>(R.id.tv_bp).text = String.format("%d/%d", item.ss, item.fz)
                return
            }
        }
    }

    private fun drawTrendChart() {
        if (trendHeartRateItems.isEmpty()) return
        val trendItems = mutableListOf<BPModel>()
        for (items in trendHeartRateItems) {
            var averageSS = 0
            var averageFZ = 0
            var validSS = 0
            var sumSS = 0
            var validFZ = 0
            var sumFZ = 0
            if (items == null || items.isEmpty()) {
                trendItems.add(BPModel(averageSS, averageFZ))
                break
            }
            for (item in items) {
                if (item.ss > 10) {
                    sumSS += item.ss
                    validSS++
                }
                if (item.fz > 10) {
                    sumFZ += item.fz
                    validFZ++
                }
            }
            if (validSS > 0 && validFZ > 0) {
                averageSS = sumSS / validSS
                averageFZ = sumFZ / validFZ
                trendItems.add(BPModel(averageSS, averageFZ))
            } else {
                trendItems.add(BPModel(0, 0))
            }
        }
        val total = when (dateType) {
            DateType.Days -> 7
            DateType.Weeks -> 28
            DateType.Months -> 90
        }
        val trendValues = mutableListOf<BPModel>()
        val reversedTrends = trendItems.reversed()
        if (trendItems.count() < total) {
            val empty = total - trendItems.count()
            for (i in 0 until empty) {
                trendValues.add(BPModel(0, 0))
            }
        }
        for (item in reversedTrends) {
            trendValues.add(item)
        }
        setupTrendData(trendValues)
        drawTrendAxis()
    }

    private fun drawDailyChart() {
        val linearLayout = findViewById<LinearLayout>(R.id.ll_hr_axis)
        linearLayout.removeAllViews()
        if (healthHeartRateItemsAll.isEmpty()) return
        val item = computeMath() ?: return
        val rangeText = "${item.maxSS}/${item.minFZ}  MmHg"
        val rangeString: Spannable = SpannableString(rangeText)
        rangeString.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            rangeText.length - 5,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        rangeString.setSpan(
            ForegroundColorSpan(ColorUtils.getColor(R.color.dark)),
            0,
            rangeText.length - 5,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        rangeString.setSpan(
            AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)),
            0,
            rangeText.length - 5,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        rangeString.setSpan(
            AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)),
            rangeText.length - 5,
            rangeText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        rangeString.setSpan(
            ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)),
            rangeText.length - 5,
            rangeText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        findViewById<TextView>(R.id.tv_range_value).text = rangeString

        val averageText = "${item.averageSS}/${item.averageFZ}  MmHg"
        val averageString: Spannable = SpannableString(averageText)
        averageString.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            averageText.length - 5,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        averageString.setSpan(
            ForegroundColorSpan(ColorUtils.getColor(R.color.dark)),
            0,
            averageText.length - 5,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        averageString.setSpan(
            AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)),
            0,
            averageText.length - 5,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        averageString.setSpan(
            AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)),
            averageText.length - 5,
            averageText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        averageString.setSpan(
            ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)),
            averageText.length - 5,
            averageText.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        findViewById<TextView>(R.id.tv_average_value).text = averageString
        drawLatest()
        setupDailyData()
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
            textView.setAutoSizeTextTypeUniformWithConfiguration(
                5,
                10,
                1,
                TypedValue.COMPLEX_UNIT_SP
            )
            textView.setTextColor(ColorUtils.getColor(R.color.light_gary))
            textView.gravity = Gravity.CENTER
            linearLayout.addView(textView)
        }
    }

    private fun selectDate(date: Date) {
        todayCalendar.time = date
        needSyncTrend = false
        healthHeartRateItemsAll.clear()
        syncDailyHeartHistory()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_bp
    }

    override fun onListener() {
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }

    private fun setupChart(chart: LineChart) {
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

        chart.setNoDataText(resources.getString(R.string.no_blood_data))

        // draw points over time
        chart.animateX(1500)
        chart.legend.isEnabled = false
    }

    private fun setupDailyData() {
        var beginBP: HealthHeartRateItem? = null
        var endBP: HealthHeartRateItem? = null
        var details = mutableListOf<HealthHeartRateItem>()
        for (item in healthHeartRateItemsAll) {
            if (item!!.fz > 10 || item!!.ss > 10) {
                if (beginBP == null) {
                    beginBP = item
                }
                details.add(item)
                endBP = item
            }
        }
        if (details.isEmpty()) {
            return
        }
        val lowTime = beginBP!!.hour * 60 + beginBP!!.minuter
        val totalIntervals =  (endBP!!.hour - beginBP.hour) * 60 + endBP.minuter - beginBP.minuter


        var timeArray = mutableListOf<String>()
        if (totalIntervals < 1) {
            timeArray.add(DateUtil.convert24To12Time(lowTime))
        } else if (totalIntervals < 10) {
            timeArray.add(DateUtil.convert24To12Time(lowTime))
            timeArray.add(DateUtil.convert24To12Time(lowTime + totalIntervals))
        } else if (totalIntervals < 60) {
            timeArray.add(DateUtil.convert24To12Time(lowTime))
            timeArray.add(DateUtil.convert24To12Time(lowTime + totalIntervals / 2))
            timeArray.add(DateUtil.convert24To12Time(lowTime + totalIntervals))
        } else {
            val sep = totalIntervals / 5
            timeArray.add(DateUtil.convert24To12Time(lowTime))
            timeArray.add(DateUtil.convert24To12Time(lowTime + sep))
            timeArray.add(DateUtil.convert24To12Time(lowTime + sep * 2))
            timeArray.add(DateUtil.convert24To12Time(lowTime + sep * 3))
            timeArray.add(DateUtil.convert24To12Time(lowTime + sep * 4))
            timeArray.add(DateUtil.convert24To12Time(lowTime + totalIntervals))
        }
        val linearLayout = findViewById<LinearLayout>(R.id.ll_hr_axis)
        for (i in 0 until timeArray.size) {
            val textView = TextView(linearLayout.context)
            val layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f
            )
            textView.layoutParams = layoutParams
            textView.text = timeArray[i]
            textView.textSize = 10f
            textView.setTextColor(ColorUtils.getColor(R.color.light_gary))
            textView.gravity = Gravity.CENTER
            linearLayout.addView(textView)
        }

        val chart = findViewById<LineChart>(R.id.lc_daily_chart)
        val values1 = ArrayList<Entry>()
        val values2 = ArrayList<Entry>()
        for (index in details.indices) {
            val item = details[index]
            val x = (index + 1.0) / details.count()
            values1.add(Entry(x.toFloat(), item!!.ss.toFloat()))
            values2.add(Entry(x.toFloat(), item.fz.toFloat()))
        }
        val set1 = LineDataSet(values1, "")
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
        set1.color = ColorUtils.getColor(R.color.red)

        // text size of values
        set1.valueTextSize = 9f

        // set the filled area
        set1.setDrawFilled(true)
        set1.fillFormatter =
            IFillFormatter { dataSet, dataProvider -> chart!!.axisLeft.axisMinimum }
        // set color of filled area
        if (Utils.getSDKInt() >= 18) {
            // drawables only supported on api level 18 and above
            val drawable = ContextCompat.getDrawable(this, R.drawable.fade_daily_ss)
            set1.fillDrawable = drawable
        } else {
            set1.fillColor = Color.WHITE
        }

        val set2 = LineDataSet(values2, "")
        set2.setDrawIcons(false)
        set2.setDrawCircleHole(false)
        set2.setDrawCircles(false)
        set2.setDrawValues(false)
//        set1.
        set2.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        // line thickness and point size
        set2.lineWidth = 1f

        // draw points as solid circles
        set2.setDrawCircleHole(false)
        set2.color = ColorUtils.getColor(R.color.pink)

        // text size of values
        set2.valueTextSize = 9f

        // set the filled area
        set2.setDrawFilled(true)
        set2.fillFormatter =
            IFillFormatter { dataSet, dataProvider -> chart!!.axisLeft.axisMinimum }
        // set color of filled area
        if (Utils.getSDKInt() >= 18) {
            // drawables only supported on api level 18 and above
            val drawable = ContextCompat.getDrawable(this, R.drawable.fade_daily_fz)
            set2.fillDrawable = drawable
        } else {
            set2.fillColor = Color.WHITE
        }

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(set1) // add the data sets
        dataSets.add(set2)
        // create a data object with the data sets
        val data = LineData(dataSets)

        // set data
        chart.data = data
        chart!!.animateX(1500)
    }

    private fun setupTrendData(trendItems: List<BPModel>) {
        val chart = findViewById<LineChart>(R.id.lc_trend_chart)
        val values1 = ArrayList<Entry>()
        val values2 = ArrayList<Entry>()
        for (index in trendItems.indices) {
            val item = trendItems[index]
            val x = (index + 1.0) / trendItems.count()
            values1.add(Entry(x.toFloat(), item.averageSS.toFloat()))
            values2.add(Entry(x.toFloat(), item.averageFZ.toFloat()))
        }
        val set1 = LineDataSet(values1, "")
        set1.setDrawIcons(false)
        set1.setDrawCircleHole(false)
        set1.setDrawCircles(false)
        set1.setDrawValues(false)
        set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        // line thickness and point size
        set1.lineWidth = 1f

        // draw points as solid circles
        set1.setDrawCircleHole(false)
        set1.color = ColorUtils.getColor(R.color.red)

        // text size of values
        set1.valueTextSize = 9f

        // set the filled area
        set1.setDrawFilled(true)
        set1.fillFormatter =
            IFillFormatter { dataSet, dataProvider -> chart!!.axisLeft.axisMinimum }
        // set color of filled area
        if (Utils.getSDKInt() >= 18) {
            // drawables only supported on api level 18 and above
            val drawable = ContextCompat.getDrawable(this, R.drawable.fade_daily_ss)
            set1.fillDrawable = drawable
        } else {
            set1.fillColor = Color.WHITE
        }

        val set2 = LineDataSet(values2, "")
        set2.setDrawIcons(false)
        set2.setDrawCircleHole(false)
        set2.setDrawCircles(false)
        set2.setDrawValues(false)
        set2.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        // line thickness and point size
        set2.lineWidth = 1f

        // draw points as solid circles
        set2.setDrawCircleHole(false)
        set2.color = ColorUtils.getColor(R.color.pink)

        // text size of values
        set2.valueTextSize = 9f

        // set the filled area
        set2.setDrawFilled(true)
        set2.fillFormatter =
            IFillFormatter { dataSet, dataProvider -> chart!!.axisLeft.axisMinimum }
        // set color of filled area
        if (Utils.getSDKInt() >= 18) {
            // drawables only supported on api level 18 and above
            val drawable = ContextCompat.getDrawable(this, R.drawable.fade_daily_fz)
            set2.fillDrawable = drawable
        } else {
            set2.fillColor = Color.WHITE
        }

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(set1) // add the data sets
        dataSets.add(set2)
        // create a data object with the data sets
        val data = LineData(dataSets)

        // set data
        chart.data = data
        chart!!.animateX(1500)
    }

}