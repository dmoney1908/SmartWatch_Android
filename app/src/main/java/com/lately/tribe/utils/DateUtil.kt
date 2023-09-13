package com.lately.tribe.utils

import java.text.SimpleDateFormat
import java.util.*

enum class DateType {
    Days,
    Weeks,
    Months
}

class DateUtil {
    companion object {
        fun getYMDHMDate(date: Date): String? {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            return simpleDateFormat.format(date)
        }

        fun getYMDDate(date: Date): String? {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            return simpleDateFormat.format(date)
        }

        fun getYMDDate2(date: Date): String? {
            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
            return simpleDateFormat.format(date)
        }

        fun getYMDDateString2(date: String): Date? {
            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
            return simpleDateFormat.parse(date)
        }


        fun getYMDate(date: Date): String? {
            val simpleDateFormat = SimpleDateFormat("MMMM,yyyy")
            return simpleDateFormat.format(date)
        }

        /**
         * 得到当前的时间(H:M:S)
         *
         * @param date
         * @return
         */
        fun getHMSTimes(date: Date?): String? {
            val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
            return simpleDateFormat.format(date)
        }

        /**
         * 计算星期几？ 取值范围从 1 - 7
         *
         * @param year 当前年份
         * @param month 当前月份
         * @param date 当前日期
         * @return
         */
        fun computeWeekNum(year: Int, month: Int, date: Int): Int {
            val isMonth1Or2 = month == 1 || month == 2
            val month1 = if (isMonth1Or2) month + 12 else month
            val year1 = if (isMonth1Or2) year - 1 else year
            val weekNum = (date + 2 * month1 + 3 * (month1 + 1) / 5 + year1 - year1 / 100 + year1 / 4 + year1 / 400) % 7
            return weekNum + 1
        }
        /**
         * 判断某一年是否是闰年
         * @param year
         */
        @JvmStatic
        fun isLeapYear(year: Int): Boolean = year % 4 == 0 && year % 100 != 0 || year % 400 == 0

        /**
         * 计算月份的天数
         *
         * @param year 当前年份
         * @param month 当前月份
         * @return
         */
        @JvmStatic
        fun computeMonthDayCount(year: Int, month: Int): Int {
            when (month) {
                2 -> return if(isLeapYear(year)) 29 else 28
                1, 3, 5, 7, 8, 10, 12 -> return 31
                4, 6, 9, 11 -> return 30
            }
            return -1
        }

        fun convert24To12Hour(hour: Int) : String {
            return if (hour > 24) {
                String.format("%02d:00 am", hour - 24)
            } else if (hour > 12) {
                String.format("%02d:00 pm", hour - 12)
            } else if (hour == 0 || hour == 24){
                return "12:00 am"
            } else if (hour == 12){
                return "12:00 pm"
            } else {
                String.format("%02d:00 am", hour)
            }
        }

        fun convert24To12Time(time: Int) : String {
            val hour = time / 60
            val minute = time % 60
            if (hour > 24) {
                return String.format("%02d:%02d am", hour - 24, minute)
            } else if (hour > 12) {
                return String.format("%02d:%02d pm", hour - 12, minute)
            } else if (hour == 0 || hour == 24){
                return String.format("12:%02d am", minute)
            } else if (hour == 12){
                return String.format("12:%02d pm", minute)
            } else {
                return String.format("%02d:%02d am", hour, minute)
            }
        }

        fun convert24To12Time(hour: Int, minute: Int) : String {
            if (hour > 24) {
                return String.format("%02d:%02d am", hour - 24, minute)
            } else if (hour > 12) {
                return String.format("%02d:%02d pm", hour - 12, minute)
            } else if (hour == 0 || hour == 24){
                return String.format("12:%02d am", minute)
            } else if (hour == 12){
                return String.format("12:%02d pm", minute)
            } else {
                return String.format("%02d:%02d am", hour, minute)
            }
        }
    }
}