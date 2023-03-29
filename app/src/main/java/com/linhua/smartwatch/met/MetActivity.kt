package com.linhua.smartwatch.met

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.blankj.utilcode.util.ColorUtils
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.linhua.smartwatch.R
import com.linhua.smartwatch.SmartWatchApplication
import com.linhua.smartwatch.base.CommonActivity
import com.linhua.smartwatch.chart.MyMarkerView
import com.linhua.smartwatch.databinding.ActivityMetBinding
import com.linhua.smartwatch.monthpicker.MonthYearPickerDialogFragment
import com.linhua.smartwatch.utils.DateType
import com.linhua.smartwatch.utils.DateUtil
import com.linhua.smartwatch.utils.DeviceManager
import com.linhua.smartwatch.view.ScrollDateView
import com.lxj.xpopup.XPopup
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException
import java.io.*
import java.util.*

class MetActivity : CommonActivity(), OnChartValueSelectedListener {
    private var dateType = DateType.Days
    private var currentMonth = Date()
    private var needSyncTrend = true
    private var todayCalendar = Calendar.getInstance()
    private var trendCalendar = Calendar.getInstance()
    private var metItemsAll = mutableListOf<MetData>()
    private var trendMetValues = mutableListOf<Int>()
    private var refreshLayout: RefreshLayout? = null

    private lateinit var binding: ActivityMetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        findViewById<TextView>(R.id.tv_time).text = DateUtil.getYMDate(Date())
        binding.vDateType.setOnClickListener {
            XPopup.Builder(this).atView(binding.vDateType).asAttachList(
                arrayOf("Days", "Weeks", "Months"), null
            ) { _, text ->
                if (dateType == DateType.valueOf(text)) return@asAttachList
                dateType = DateType.valueOf(text)
                findViewById<TextView>(R.id.iv_date_type).text = text
                trendCalendar = Calendar.getInstance()
                trendMetValues.clear()
                syncTrendMetHistory()
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
                refreshLayout!!.finishRefresh(true)
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
        setupChart(binding.bcTrendChart)
        syncTodayMetData()
    }

    private fun syncTodayMetData() {
        if (!DeviceManager.isSDKAvailable || DeviceManager.getConnectedDevice() == null) {
            refreshLayout!!.finishRefresh(false)
        }
        loadMets()
        BleSdkWrapper.getMettInfo(object : OnLeWriteCharacteristicListener() {
            override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                val map = handlerBleDataResult.data as Map<Int, Int>
                var metItems = mutableListOf<MetData>()
                for (key in map.keys) {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DATE, -1 * key)
                    val date = DateUtil.getYMDDate(calendar.time)
                    var metData = date?.let { MetData(it, map[key]!!) }
                    if (metData != null) {
                        metItems.add(metData)
                    }
                }
                metItems = metItems.reversed() as MutableList<MetData>
                for (met1 in metItems) {
                    var found = false
                    for (index in metItemsAll.indices) {
                        val met2 = metItemsAll[index]
                        if (met1.date == met2.date) {
                            found = true
                            metItemsAll[index] = met1
                            break
                        }
                    }
                    if (!found) {
                        metItemsAll.add(met1)
                    }
                }
                var sum = 0
                for (met in metItems) {
                    sum += met.value
                }
                binding.tvMetValue.text = sum.toString()
                if (needSyncTrend) {
                    syncTrendMetHistory()
                }
                refreshLayout!!.finishRefresh(true)
                saveMets()
            }

            override fun onFailed(e: WriteBleException) {
                if (needSyncTrend) {
                    syncTrendMetHistory()
                }
                refreshLayout!!.finishRefresh(false)
            }
        })
    }

    fun saveMets() {
        val devicesSP: SharedPreferences = SmartWatchApplication.instance.getSharedPreferences("mets", MODE_PRIVATE)
        try {
            val var2 = ByteArrayOutputStream()
            var var3: ObjectOutputStream? = null
            var3 = ObjectOutputStream(var2)
            var3.writeObject(metItemsAll!!)
            val var4 = String(Base64.encode(var2.toByteArray(), 0))
            var3.close()
            devicesSP.edit().putString("mets", var4).apply()
        } catch (var5: IOException) {
            var5.printStackTrace()
        }
    }

    fun loadMets() {
        val devicesSP: SharedPreferences = SmartWatchApplication.instance.getSharedPreferences("mets", MODE_PRIVATE)
        try {
            var var2 = devicesSP.getString("mets", "")
            if (var2 == null || var2.isEmpty()) {
                return
            }
            val var3 = Base64.decode(var2.toByteArray(), 0)
            val var4 = ByteArrayInputStream(var3)
            val var5 = ObjectInputStream(var4)
            val var1 = var5.readObject() as List<MetData>
            var5.close()
            for (item in var1) {
                metItemsAll.add(item)
            }
        } catch (var6: ClassNotFoundException) {
            var6.printStackTrace()
        } catch (var6: IOException) {
            var6.printStackTrace()
        }
    }

    private fun syncTrendMetHistory() {
        var dataCount = 7
        when (dateType) {
            DateType.Days -> {
                dataCount = 7
            }
            DateType.Weeks -> {
                dataCount = 28
            }

            DateType.Months -> {
                dataCount = 90
            }
        }
        trendMetValues.clear()
        var x = 0
        for (item in metItemsAll.reversed()) {
            if (x < dataCount) {
                trendMetValues.add(item.value)
            } else {
                break
            }
            x += 1
        }
        x = trendMetValues.count()
        if (x < dataCount) {
            for (i in 0 until  dataCount - x) {
                trendMetValues.add(0)
            }
        }

        drawTrendChart()
    }

    private fun drawTrendChart() {
        if (trendMetValues.isEmpty()) return
        trendMetValues = trendMetValues.reversed().toMutableList()
        val trendItems = mutableListOf<Int>()
        when (dateType) {
            DateType.Days -> {
                trendItems.addAll(trendMetValues)
            }
            DateType.Weeks -> {
                var sum = 0
                for (index in trendMetValues.indices) {
                    if (index % 7 == 0) {
                        trendItems.add(sum)
                        sum = 0
                    }
                    sum += trendMetValues[index]
                }
                trendItems.add(sum)
            }

            DateType.Months -> {
                var sum = 0
                for (index in trendMetValues.indices) {
                    if (index % 30 == 0) {
                        trendItems.add(sum)
                        sum = 0
                    }
                    sum += trendMetValues[index]
                }
                trendItems.add(sum)
            }
        }

        setupTrendData(trendItems)
        drawTrendAxis()
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
        val calendar = Calendar.getInstance()
        calendar.time = date
        var sum = 0
        for (i in 0..6) {
            if (i > 0) {
                calendar.add(Calendar.DATE, -1)
            }
            val date = DateUtil.getYMDDate(calendar.time)

            for (met in metItemsAll.reversed()) {
                if (date == met.date) {
                    sum += met.value
                    break
                }
            }
        }

        binding.tvMetValue.text = sum.toString()

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
        chart.isDragEnabled = true
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

        chart.setNoDataText(resources.getString(R.string.no_met_data))

        // draw points over time
        chart.animateX(1500)
        chart.legend.isEnabled = false
    }

    private fun setupTrendData(trendItems: List<Int>) {
        val chart = binding.bcTrendChart
        val values = ArrayList<BarEntry>()
        for (index in trendItems.indices) {
            val item = trendItems[index]
            val x = (index + 1.0)
            values.add(BarEntry(x.toFloat(), item.toFloat()))
        }
        val set1 = BarDataSet(values, "")
        set1.setDrawIcons(false)
        set1.setDrawValues(false)
        set1.color = ColorUtils.getColor(R.color.primary_blue)

        // text size of values
        set1.valueTextSize = 9f

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(set1) // add the data sets

        // create a data object with the data sets
        val data = BarData(dataSets)
        data.barWidth = 0.7f
        chart.data = data
        chart!!.animateX(1500)
    }
}