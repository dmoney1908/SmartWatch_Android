package com.linhua.smartwatch.utils

import android.annotation.TargetApi
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.linhua.smartwatch.activity.MyAppcation

/**
 * Created by lyw.
 *
 * @author: lyw
 * @package: com.id.app.comm.lib.utils
 * @description: ${TODO}{ 类注释}
 * @date: 2018/9/21 0021
 */
class PermissionUtil {
    fun setRequsetResult(requsetResult: RequsetResult?) {
        this.requsetResult = requsetResult
    }

    private var requsetResult: RequsetResult? = null
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        val deniedPermissions: MutableList<String> = ArrayList()
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i])
            }
        }
        if (deniedPermissions.size > 0) {
            requsetResult!!.requestPermissionsFail(requestCode)
        } else {
            requsetResult!!.requestPermissionsSuccess(requestCode)
        }
    }

    interface RequsetResult {
        fun requestPermissionsSuccess(requestCode: Int)
        fun requestPermissionsFail(requestCode: Int)
    }

    companion object {
        val isOverMarshmallow: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

        /**
         * 检查是否有权限
         * @param permission
         * @return
         */
        @TargetApi(value = Build.VERSION_CODES.M)
        fun findDeniedPermissions(vararg permission: String): List<String> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return ArrayList()
            }
            val denyPermissions: MutableList<String> = ArrayList()
            for (value in permission) {
                if (MyAppcation.instance?.let {
                        ContextCompat.checkSelfPermission(
                            it,
                            value
                        )
                    } != PackageManager.PERMISSION_GRANTED
                ) {
                    denyPermissions.add(value)
                }
            }
            return denyPermissions
        }

        /**
         * Return whether the app can draw on top of other apps.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        @get:RequiresApi(api = Build.VERSION_CODES.M)
        val isGrantedDrawOverlays: Boolean
            get() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val aom = MyAppcation.instance?.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                        ?: return false
                    val mode = MyAppcation.instance?.packageName?.let {
                        aom.checkOpNoThrow(
                            "android:system_alert_window",
                            Process.myUid(),
                            it
                        )
                    }
                    return mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED
                }
                return Settings.canDrawOverlays(MyAppcation.instance)
            }

        /**
         * 检测权限，如果返回true,有权限 false 无权限
         * @param permission 权限
         * @return 是否有权限
         */
        fun checkSelfPermission(vararg permission: String?): Boolean {
            return findDeniedPermissions(*permission as Array<out String>).isEmpty()
        }

        @TargetApi(Build.VERSION_CODES.M)
        private fun startWriteSettingsActivity(activity: Activity, requestCode: Int) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + MyAppcation.instance?.packageName)
            if (!isIntentAvailable(intent)) {
                launchAppDetailsSettings()
                return
            }
            activity.startActivityForResult(intent, requestCode)
        }

        /**
         * 打开应用具体设置
         * Launch the application's details settings.
         */
        fun launchAppDetailsSettings() {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + MyAppcation.instance?.packageName)
            if (!isIntentAvailable(intent)) return
            MyAppcation.instance?.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        private fun isIntentAvailable(intent: Intent): Boolean {
            val size = MyAppcation.instance?.packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)?.size
            return if (size != null) {
                size > 0
            } else {
                false
            }
        }

        /**
         * 申请权限
         * @param object
         * @param requestCode
         * @param permissions
         */
        @TargetApi(value = Build.VERSION_CODES.M)
        fun requestPermissions(`object`: Any, requestCode: Int, vararg permissions: String?) {
            if (!isOverMarshmallow) {
                return
            }
            var activity: Activity? = null
            if (`object` is Activity) {
                activity = `object`
            } else if (`object` is Fragment) {
                activity = `object`.activity
            }
            val deniedPermissions = findDeniedPermissions(*permissions as Array<out String>)
            if (deniedPermissions.isNotEmpty()) {
                if (`object` is Activity) {
                    `object`.requestPermissions(deniedPermissions.toTypedArray(), requestCode)
                } else if (`object` is Fragment) {
                    `object`.requestPermissions(deniedPermissions.toTypedArray(), requestCode)
                } else {
                    throw IllegalArgumentException(`object`.javaClass.name + " is not supported")
                }
            }
        }
    }
}