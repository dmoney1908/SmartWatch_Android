package com.linhua.smartwatch.sleep

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import android.widget.ImageView
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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
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
import com.linhua.smartwatch.utils.ScreenUtil
import com.linhua.smartwatch.view.ScrollDateView
import com.lxj.xpopup.XPopup
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.zhj.bluetooth.zhjbluetoothsdk.bean.HealthSleepItem
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt


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
    private var refreshLayout: RefreshLayout? = null

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
        findViewById<ImageView>(R.id.base_title_back).setOnClickListener {
            onBackPressed()
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
        findViewById<ScrollDateView>(R.id.rl_scroll).selectCallBack = { date: Date ->
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
                if (thisCalendar.get(Calendar.YEAR) == year && thisCalendar!!.get(Calendar.MONTH) == monthOfYear) {
                    selectDay = calendar!!.get(Calendar.DATE)
                }
                calendar.set(Calendar.DATE, selectDay)
                selectDate(calendar.time)
            }

            dialogFragment.show(supportFragmentManager, null)
        }

        setupPieChart(findViewById<PieChart>(R.id.pc_daily_chart))
        setupTrendChart(findViewById<LineChart>(R.id.lc_trend_chart))

        val linearLayout = findViewById<LinearLayout>(R.id.rl_step_title)
        linearLayout.removeAllViews()
        healthSleepItems.clear()
        syncDailySleepHistory()
    }

    private fun syncDailySleepHistory() {
        if (!DeviceManager.isSDKAvailable || DeviceManager.getConnectedDevice() == null) {
            refreshLayout!!.finishRefresh(false)
        }
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
                                healthSleepItems.add(sleepItems)
                            }
                            if (dailyDateIndex >= 2) {
                                drawDailyChart()
                                if (needSyncTrend) {
                                    syncTrendSleepHistory()
                                }
                                refreshLayout!!.finishRefresh(true)
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
                    refreshLayout!!.finishRefresh(false)
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
                            syncTrendSleepHistory()
                        } else {
                            drawTrendChart()
                        }
                    }
                }

                override fun onFailed(e: WriteBleException) {}
            })
    }


    private fun computeMath(): Triple<Int, Int, Int>? {
        var deep = 0
        var light = 0
        var wide: Int = 0
        if (healthSleepItems.isEmpty()) {
            return Triple(0, 0, 0)
        }
        val sleepItems = healthSleepItems.first()

        for (item in sleepItems!!) {
            when (item.sleepStatus) {
                2 -> {
                    light += 10
                }
                3 -> {
                    deep += 10
                }
                4 -> {
                    wide += 10
                }
            }
        }
        return Triple(deep, light, wide)
    }

    private fun drawLatest(total: Int) {
        val hour = total / 60
        val minu = total % 60
        findViewById<TextView>(R.id.tv_duration_hr).text = String.format("%02d", hour)
        findViewById<TextView>(R.id.tv_duration_min).text = String.format("%02d", minu)
    }

    private fun drawTrendChart() {
        if (trendSleepItems.isEmpty()) return
        val trendItems = mutableListOf<Int>()
        for (items in trendSleepItems) {
            var sum = 0
            if (items == null || items.isEmpty()) {
                trendItems.add(0)
                break
            }
            for (item in items) {
                if (item.sleepStatus == 2 || item.sleepStatus == 3 || item.sleepStatus == 4) {
                    sum += 10
                }
            }
            trendItems.add(sum)
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

    private fun makeAttributeString(minute: Int): Spannable {
        val hour = minute / 60
        val minu = minute % 60
        if (hour == 0) {
            val minText = "${minu}min"
            val minString: Spannable = SpannableString(minText)
            minString.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                minText.length - 3,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
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
            return minString
        } else if (minu == 0) {
            val minText = "${hour}hr"
            val minString: Spannable = SpannableString(minText)
            minString.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                minText.length - 2,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            minString.setSpan(
                ForegroundColorSpan(ColorUtils.getColor(R.color.dark)),
                0,
                minText.length - 2,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            minString.setSpan(
                AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)),
                0,
                minText.length - 2,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            minString.setSpan(
                AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)),
                minText.length - 2,
                minText.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            minString.setSpan(
                ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)),
                minText.length - 2,
                minText.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            return minString
        } else {
            val minText = "${minu}min"
            val minString: Spannable = SpannableString(minText)
            minString.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                minText.length - 3,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
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

            val hourText = "${hour}hr "
            val hourString: Spannable = SpannableString(hourText)
            hourString.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                hourText.length - 3,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            hourString.setSpan(
                ForegroundColorSpan(ColorUtils.getColor(R.color.dark)),
                0,
                hourText.length - 3,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            hourString.setSpan(
                AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size20dp)),
                0,
                hourText.length - 3,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            hourString.setSpan(
                AbsoluteSizeSpan(resources.getDimensionPixelSize(R.dimen.size12dp)),
                hourText.length - 3,
                hourText.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            hourString.setSpan(
                ForegroundColorSpan(ColorUtils.getColor(R.color.light_gray)),
                hourText.length - 3,
                hourText.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )

            val summary = SpannableStringBuilder()
            summary.append(hourString)
            summary.append(minString)
            return summary
        }
    }

    private fun drawDailyChart() {
        val linearLayout = findViewById<LinearLayout>(R.id.rl_step_title)
        linearLayout.removeAllViews()
        val relativeLayout = findViewById<RelativeLayout>(R.id.rl_step_chart)
        relativeLayout.removeAllViews()
        if (healthSleepItems.isEmpty()) {
            findViewById<TextView>(R.id.tv_duration_hr).text = "-"
            findViewById<TextView>(R.id.tv_duration_min).text = "-"
            val chart = findViewById<PieChart>(R.id.pc_daily_chart)
            chart.data = null
            chart.notifyDataSetChanged()
            return
        }
        val item = computeMath() ?: return
        val (deep, light, wide) = item
        findViewById<TextView>(R.id.tv_deepsleep_value).text = makeAttributeString(deep)
        findViewById<TextView>(R.id.tv_light_value).text = makeAttributeString(light)
        findViewById<TextView>(R.id.tv_awake_value).text = makeAttributeString(wide)
        drawStepChart()
        drawLatest(deep + light + wide)
        setupPieData()
    }

    private fun drawStepChart() {
        val linearLayout = findViewById<LinearLayout>(R.id.rl_step_title)
        val relativeLayout = findViewById<RelativeLayout>(R.id.rl_step_chart)
        if (healthSleepItems.isEmpty()) {
            return
        }

        var lastModel: SleepStepModel? = null
        var lastType = 0
        val stepSleepValues = mutableListOf<SleepStepModel>()
        var beginDate : Int? = null
        var endDate : Int? = null
        var baseHour = 0
        if (healthSleepItems.count() >= 2) {
            val yesterday = healthSleepItems[1]

            //11点到最后
            for (item in yesterday!!) {
                if (item.hour < 22) {
                    continue
                }
                when (item.sleepStatus) {
                    2, 3, 4 -> {

                        if (lastType != item.sleepStatus) {
                            lastModel = SleepStepModel()
                            lastModel.duration = item.offsetMinute
                            lastModel.type = item.sleepStatus
                            lastModel.beginTime = (item.hour - 22) * 60 + item.minuter
                            stepSleepValues.add(lastModel)
                        } else {
                            if (lastModel != null) {
                                lastModel.duration += item.offsetMinute
                            }
                        }
                        lastType = item.sleepStatus
                        if (beginDate == null) {
                            beginDate = lastModel!!.beginTime
                            baseHour = 22
                        }
                        endDate = lastModel!!.beginTime + lastModel.duration
                    }
                    else -> {
                        lastType = 0
                        lastModel = null
                    }
                }
            }
        }
        var delta = 0
        if (baseHour == 22) {
            delta = 120
        }
        val today = healthSleepItems.first()
        //0点到9:00
        for (item in today!!) {
            if (item.hour >= 9) {
                continue
            }
            when (item.sleepStatus) {
                2, 3, 4 -> {
                    if (lastType != item.sleepStatus) {
                        lastModel = SleepStepModel()
                        lastModel.duration = item.offsetMinute
                        lastModel.type = item.sleepStatus
                        lastModel.beginTime = item.hour * 60 + item.minuter + delta
                        stepSleepValues.add(lastModel)
                    } else {
                        if (lastModel != null) {
                            lastModel.duration += item.offsetMinute
                        }
                    }
                    lastType = item.sleepStatus
                    if (beginDate == null) {
                        beginDate = lastModel!!.beginTime
                        baseHour = 0
                    }
                    endDate = lastModel!!.beginTime + lastModel.duration
                }
                else -> {
                    lastType = 0
                    lastModel = null
                }
            }
        }
        if (stepSleepValues.isEmpty()) return
        val hour = beginDate!! / 60
        val sep = ceil((endDate!! - hour * 60) / 240.0).toInt()
        val timeArray = arrayOf(
            DateUtil.convert24To12Hour(baseHour + hour),
            DateUtil.convert24To12Hour(baseHour + hour + sep),
            DateUtil.convert24To12Hour(baseHour + hour + sep * 2),
            DateUtil.convert24To12Hour(baseHour + hour + sep * 3),
            DateUtil.convert24To12Hour(baseHour + hour + sep * 4)
        )
        for (i in timeArray.indices) {
            val textView = TextView(linearLayout.context)
            val layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0f
            )
            textView.layoutParams = layoutParams
            textView.text = timeArray[i]
            textView.textSize = 10f
            textView.setTextColor(ColorUtils.getColor(R.color.light_gary))
            textView.gravity = Gravity.CENTER
            linearLayout.addView(textView)
        }

        val totalMinute = sep * 4 * 60F

        val px = ScreenUtil.dp2px(1F, this)
        for (item in stepSleepValues) {
            val view = View(relativeLayout.context)
            view.setBackgroundResource(R.drawable.step_corner_shape)
            item.beginTime = item.beginTime - hour * 60
            val width: Int = (item.duration / totalMinute * relativeLayout.width + 2 * px).toInt()
            val height: Int = (15F / 200 * relativeLayout.height).toInt()
            val layoutParams = RelativeLayout.LayoutParams(
                width,
                height
            )
            item.x = (item.beginTime / totalMinute * relativeLayout.width - px).toInt()
            layoutParams.marginStart = item.x
            view.layoutParams = layoutParams
            val drawable = view.background as GradientDrawable
            when (item.type) {
                2 -> {
                    item.y = (88F / 200 * relativeLayout.height).toInt()
                    layoutParams.topMargin = item.y
                    drawable.setColor(ColorUtils.getColor(R.color.pink))
                }
                3 -> {
                    item.y = (26F / 200 * relativeLayout.height).toInt()
                    layoutParams.topMargin = item.y
                    drawable.setColor(ColorUtils.getColor(R.color.purple_200))
                }
                4 -> {
                    item.y = (166F / 200 * relativeLayout.height).toInt()
                    layoutParams.topMargin = item.y
                    drawable.setColor(ColorUtils.getColor(R.color.orange))
                }
            }
            item.height = height
            item.width = width
            relativeLayout.addView(view)
        }
        for (index in stepSleepValues.indices) {
            if (index == 0) {
                continue
            }
            val leftNode = stepSleepValues[index - 1]
            val curNode = stepSleepValues[index]
            if (leftNode.right < curNode.left) {
                continue
            }
            val sepView = View(relativeLayout.context)
            val width: Int = 2 * px
            if (leftNode.bottom < curNode.top) {
                val height: Int = curNode.top - leftNode.bottom + 4 * px
                val layoutParams = RelativeLayout.LayoutParams(
                    width,
                    height
                )
                layoutParams.marginStart = leftNode.right - 2 * px
                layoutParams.topMargin = leftNode.bottom - 2 * px
                sepView.layoutParams = layoutParams

                val gd = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(getSleepColor(leftNode.type), getSleepColor(curNode.type))
                )
                gd.cornerRadius = 0f
                sepView.background = gd
            } else {
                val height: Int = leftNode.top - curNode.bottom + 4 * px
                val layoutParams = RelativeLayout.LayoutParams(
                    width,
                    height
                )
                layoutParams.marginStart = curNode.left
                layoutParams.topMargin = curNode.bottom - 2 * px
                sepView.layoutParams = layoutParams
                val gd = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(getSleepColor(curNode.type), getSleepColor(leftNode.type))
                )
                gd.cornerRadius = 0f
                sepView.background = gd
            }
            relativeLayout.addView(sepView)
        }
    }

    private fun getSleepColor(type: Int): Int {
        return when (type) {
            2 -> {
                ColorUtils.getColor(R.color.pink)
            }
            3 -> {
                ColorUtils.getColor(R.color.purple_200)
            }
            4 -> {
                ColorUtils.getColor(R.color.orange)
            }
            else -> {
                ColorUtils.getColor(R.color.pink)
            }
        }
    }

    private fun drawDailyAxis() {
        val linearLayout = findViewById<LinearLayout>(R.id.rl_step_title)
        linearLayout.removeAllViews()
        val timeArray = arrayOf("23:00", "01:00", "04:00", "06:00", "08:00")
        for (i in timeArray.indices) {
            val textView = TextView(linearLayout.context)
            val layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0f
            )
            textView.layoutParams = layoutParams
            textView.text = timeArray[i]
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

            yAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value.roundToInt() == value.toInt()) {
                        String.format("%d hrs", value.toInt())
                    } else {
                        String.format("%.1f hrs", value)
                    }
                }
            }
            yAxis.axisLineColor = ColorUtils.getColor(R.color.light_gary)
            yAxis.textColor = ColorUtils.getColor(R.color.light_gary)
        }
        yAxis.setDrawLimitLinesBehindData(false)
        xAxis.setDrawLimitLinesBehindData(false)

        // draw points over time
        chart.animateX(1500)
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
            values.add(Entry(x.toFloat(), item.toFloat() / 60))
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