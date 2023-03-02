package com.linhua.smartwatch.utils

import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.text.TextPaint
import android.text.TextUtils
import android.util.TypedValue
import android.widget.TextView
import com.linhua.smartwatch.activity.MyAppcation
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Describe 工具栏
 */
object CommonUtil {
    /**
     * 是否是24小时
     *
     * @return
     */
    fun is24Hour(): Boolean {
//        int timeStyle = (int) SPUtils.get(Constant.TIME_STYLE,0);
        val is24: Boolean
        //        if (timeStyle == 0) {//跟随系统
        val cv: ContentResolver? = MyAppcation.instance?.contentResolver
        // 获取当前系统设置
        val time_12_24 = Settings.System.getString(
            cv,
            Settings.System.TIME_12_24
        )
        is24 = if ("24" == time_12_24) true else false
        //        }else{
//            is24 = timeStyle == Constants.TIME_MODE_24;
//        }
        return true
    }

    fun format24To12(hour: Int): Int {
        var h = hour % 12
        h = if (hour == 12) {
            if (h == 0) 12 else h
        } else {
            if (h == 0) 0 else h
        }
        return h
    }

    fun isAM(hour: Int): Boolean {
        return hour < 12
    }

    /**
     * 将一天中的分钟序列数变为对应的hh:mm形式
     *
     * @param mins 00:00为第一分钟， mins = h * 60 + m;范围1~1440
     * @return
     */
    fun timeFormatter(mins: Int, is24: Boolean, amOrPm: Array<String>?, isUnit: Boolean): String {
        var mins = mins
        if (mins >= 0 && mins < 1440) {
            val h = getHourAndMin(mins, is24)[0]
            val min = mins % 60
            return if (is24) {
                String.format("%1$02d:%2$02d", if (h == 24) 0 else h, min)
            } else {
                var m = ""
                if (isUnit) {
                    m = if (amOrPm != null) {
                        if (mins <= 12 * 60) amOrPm[0] else amOrPm[1]
                    } else {
                        if (mins <= 12 * 60) "am" else "pm"
                    }
                }
                //                if(m.equals("下午")||m.equals("上午")){
                //                    return m+ String.format("%1$02d:%2$02d", h == 24 ? 0 : h, min);
                //                }else {
                String.format("%1$02d:%2$02d", if (h == 24) 0 else h, min) + m
                //                }
            }
        } else if (mins >= 1440) {
            mins -= 1440
            var h = 0
            var min = 0
            if (mins > 0) {
                h = getHourAndMin(mins, is24)[0]
                min = mins % 60
            }
            return if (is24) {
                String.format("%1$02d:%2$02d", if (h == 24) 0 else h, min)
            } else {
                var m = ""
                if (isUnit) {
                    m = if (amOrPm != null) {
                        if (mins <= 12 * 60) amOrPm[0] else amOrPm[1]
                    } else {
                        if (mins <= 12 * 60) "am" else "pm"
                    }
                }
                String.format("%1$02d:%2$02d", if (h == 24) 0 else h, min) + m
            }
        }

//        Log.e("Util", "timeFormatter Error : mins is out of range [0 , 1440).");
//        return "--:--";
        return "00:00"
    }

    /**
     * 将一天中的分钟序列数变为对应的hh:mm形式
     *
     * @param mins 00:00为第一分钟， mins = h * 60 + m;范围1~1440
     * @return
     */
    fun timeFormatter(
        mins: Int,
        is24: Boolean,
        amOrPm: Array<String>?,
        isUnit: Boolean,
        isStart: Boolean
    ): String {
        var mins = mins
        if (mins >= 0 && mins < 1440) {
            var h = getHourAndMin(mins, is24)[0]
            var min = mins % 60
            if (!isStart && min != 0) {
                h += 1
            }
            min = 0
            return if (is24) {
                String.format("%1$02d:%2$02d", if (h == 24) 0 else h, min)
            } else {
                var m = ""
                if (isUnit) {
                    m = if (amOrPm != null) {
                        if (mins < 12 * 60) amOrPm[0] else amOrPm[1]
                    } else {
                        if (mins < 12 * 60) "am" else "pm"
                    }
                }
                m + String.format("%1$02d:%2$02d", if (h == 24) 0 else h, min)
            }
        } else if (mins >= 1440) {
            mins -= 1440
            var h = 0
            var min = 0
            if (mins > 0) {
                h = getHourAndMin(mins, is24)[0]
                min = mins % 60
            }
            if (!isStart && min != 0) {
                h += 1
            }
            min = 0
            return if (is24) {
                String.format("%1$02d:%2$02d", if (h == 24) 0 else h, min)
            } else {
                var m = ""
                if (isUnit) {
                    m = if (amOrPm != null) {
                        if (mins < 12 * 60) amOrPm[0] else amOrPm[1]
                    } else {
                        if (mins < 12 * 60) "am" else "pm"
                    }
                }
                m + String.format("%1$02d:%2$02d", if (h == 24) 0 else h, min)
            }
        }

//        Log.e("Util", "timeFormatter Error : mins is out of range [0 , 1440).");
//        return "--:--";
        return "00:00"
    }

    fun getHourAndMin(mins: Int, is24: Boolean): IntArray {
        var h = mins / 60
        // 0 ,12,24都是12点 ， 下午的-12
        h = if (is24) h else if (h % 12 == 0) 12 else if (h > 12) h - 12 else h
        return intArrayOf(h, mins % 60)
    }
    /**
     * @param h
     * @param min
     * @param is24
     * @return
     */
    /**
     * @param time 00:00
     * @param is24
     * @return
     */
    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    fun isOPen(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return if (gps || network) {
            true
        } else false
    }

    /**
     * 强制帮用户打开GPS
     *
     * @param context
     */
    fun openGPS(context: Context?) {
        val GPSIntent = Intent()
        GPSIntent.setClassName(
            "com.android.settings",
            "com.android.settings.widget.SettingsAppWidgetProvider"
        )
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE")
        GPSIntent.data = Uri.parse("custom:3")
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send()
        } catch (e: CanceledException) {
            e.printStackTrace()
        }
    }
    /**
     * 根据星期开始日获取周
     * 如果开始日为周日则 返回 日 一 二 三 四
     * 如果开始日为 六 则返回 六 日 一 二
     * 如果开始日为一 则返回 一 二 三 四
     *
     * @param context
     * @param weekStartDay
     * @return
     */
    /**
     * 把发送闹钟的week转换为显示的week
     * 发送闹钟的week 固定是从星期一开始的
     * 显示的week 根据开始星期日决定的
     *
     * @return 返回星期一为开始日的数组
     * @startWeek 0:周六，1：周日，2：周一
     */
    fun alarmToShowAlarm(week: BooleanArray, startWeek: Int): BooleanArray {
        var tempAlarm = BooleanArray(7)
        if (startWeek == 0) {
            tempAlarm[0] = week[2]
            tempAlarm[1] = week[3]
            tempAlarm[2] = week[4]
            tempAlarm[3] = week[5]
            tempAlarm[4] = week[6]
            tempAlarm[5] = week[0]
            tempAlarm[6] = week[1]
        } else if (startWeek == 1) {
            tempAlarm[0] = week[1]
            tempAlarm[1] = week[2]
            tempAlarm[2] = week[3]
            tempAlarm[3] = week[4]
            tempAlarm[4] = week[5]
            tempAlarm[5] = week[6]
            tempAlarm[6] = week[0]
        } else {
            tempAlarm = Arrays.copyOf(week, week.size)
        }
        return tempAlarm
    }

    /**
     * @param week
     * @param startWeek 0:周六，1：周日，2：周一
     * @return 将星期一为开始日的数组，转换成其他一种
     */
    fun alarmToShowAlarm2(week: BooleanArray, startWeek: Int): BooleanArray {
        var tempAlarm = BooleanArray(7)
        if (startWeek == 0) {
            tempAlarm[0] = week[5]
            tempAlarm[1] = week[6]
            tempAlarm[2] = week[0]
            tempAlarm[3] = week[1]
            tempAlarm[4] = week[2]
            tempAlarm[5] = week[3]
            tempAlarm[6] = week[4]
        } else if (startWeek == 1) {
            tempAlarm[0] = week[6]
            tempAlarm[1] = week[0]
            tempAlarm[2] = week[1]
            tempAlarm[3] = week[2]
            tempAlarm[4] = week[3]
            tempAlarm[5] = week[4]
            tempAlarm[6] = week[5]
        } else {
            tempAlarm = Arrays.copyOf(week, week.size)
        }
        return tempAlarm
    }

    /**
     * 是否有轨迹
     *
     * @param type
     * @return
     */
    fun hasOrbit(type: Int): Boolean {
        //0x01;// 走路
        //0x02;// 跑步
        //0x03;// 骑行
        //0x04;// 徒步
        val types = intArrayOf(1, 2, 3, 4)
        for (t in types) {
            if (t == type) {
                return true
            }
        }
        return false
    }

    fun noHeartRate(s: String): String {
        return if (TextUtils.isEmpty(s) || s == "0") {
            "--"
        } else s + ""
    }

    fun noBloodPressure(systolicPressure: Int, diastolicPressure: Int): String {
        return if (systolicPressure == 0 || diastolicPressure == 0) {
            "--/--"
        } else "$systolicPressure/$diastolicPressure"
    }

    fun noPace(speed: Int): String {
        if (speed == 0) {
            return "--"
        }
        val avgPace = StringBuffer()
        avgPace.append(speed / 60)
        avgPace.append("'")
        avgPace.append(speed % 60) //转换字符串
        avgPace.append("\"")
        return avgPace.toString()
    }

    fun adjustTvTextSize(tv: TextView, maxWidth: Int, text: String?) {
        val avaiWidth = maxWidth - tv.paddingLeft - tv.paddingRight - 10
        if (avaiWidth <= 0) {
            return
        }
        val textPaintClone = TextPaint(tv.paint)
        // note that Paint text size works in px not sp
        var trySize = textPaintClone.textSize
        while (textPaintClone.measureText(text) > avaiWidth) {
            trySize--
            textPaintClone.textSize = trySize
        }
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize)
    }

    private var df: DecimalFormat? = null
    private var decimalFormat: DecimalFormat? = null

    init {
        Locale.setDefault(Locale.CHINA)
        df = DecimalFormat("#,###")
        decimalFormat = DecimalFormat("###,###,###,##0.00")
    }

    fun formatThree(value: Int): String {
        return df!!.format(value.toLong())
    }

    fun formatThree(value: Float): String {
        return df!!.format(value.toDouble())
    }

    fun formatNumber(num: Int): String {
        val formatNum: String
        formatNum = if (num > 10000) {
            "10,000+"
        } else {
            df!!.format(num.toLong())
        }
        return formatNum
    }

    /**
     * 保留两位并且三位用“，”隔开
     *
     * @param num
     * @return
     */
    fun formatDistance(num: Float): String {
        return decimalFormat!!.format(num.toDouble())
    }

    fun getFormat(format: String?): SimpleDateFormat {
        return SimpleDateFormat(format)
    }

    /**
     * 计算月数
     *
     * @return
     */
    private fun calculationDaysOfMonth(year: Int, month: Int): Int {
        var day = 0
        when (month) {
            1, 3, 5, 7, 8, 10, 12 -> day = 31
            4, 6, 9, 11 -> day = 30
            2 -> day =
                if (year % 100 == 0) if (year % 400 == 0) 29 else 28 else if (year % 4 == 0) 29 else 28
        }
        return day
    }

    /**
     * 目标时间选择列表（无单位）
     * @return
     */
    val timeList: List<Float>
        get() {
            val times: MutableList<Float> = ArrayList()
            times.add(5f)
            var i = 10
            while (i <= 6000) {
                times.add(i * 1f)
                i += 10
            }
            return times
        }

    /**
     * 目标距离选择列表（无单位）
     * @return
     */
    val distanceList: List<Float>
        get() {
            val distances: MutableList<Float> = ArrayList()
            distances.add(0.5f)
            for (i in 1..100) {
                distances.add(i * 1f)
            }
            return distances
        }

    /**
     * 目标卡路里选择列表（无单位）
     * @return
     */
    fun gettCalorieList(): List<Float> {
        val distances: MutableList<Float> = ArrayList()
        var i = 300
        while (i <= 9000) {
            distances.add(i * 1f)
            i += 300
        }
        return distances
    }
}