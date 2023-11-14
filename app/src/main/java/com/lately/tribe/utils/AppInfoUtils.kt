package com.lately.tribe.utils

import android.content.Context
import android.content.pm.PackageManager

class AppInfoUtils {

    companion object {
        fun getAppVersionName(context: Context): String {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                return packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return ""
        }

        fun getAppVersionCode(context: Context): Int {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    packageInfo.versionCode
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return -1
        }
    }
}
