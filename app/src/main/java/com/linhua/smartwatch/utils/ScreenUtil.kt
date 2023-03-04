package com.linhua.smartwatch.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast

/**
 * 获得屏幕相关的辅助类
 *
 * @author zhy
 */
class ScreenUtil private constructor() {
    init {
        /* cannot be instantiated */
        throw UnsupportedOperationException("cannot be instantiated")
    }

    companion object {
        private var screenW = 0
        private var screenH = 0
        private var screenDensity = 0f
        fun initScreen(mActivity: Activity) {
            val metric = DisplayMetrics()
            mActivity.windowManager.defaultDisplay.getMetrics(metric)
            screenW = metric.widthPixels
            screenH = metric.heightPixels
            screenDensity = metric.density
        }

        /**
         * 获得屏幕高度
         *
         * @param context
         * @return
         */
        fun getScreenWidth(context: Context): Int {
            val wm = context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val outMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(outMetrics)
            return outMetrics.widthPixels
        }

        /**
         * 获得屏幕宽度
         *
         * @param context
         * @return
         */
        fun getScreenHeight(context: Context): Int {
            val wm = context
                .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val outMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(outMetrics)
            return outMetrics.heightPixels
        }

        /**
         * 获得状态栏的高度
         *
         * @param context
         * @return
         */
        fun getStatusHeight(context: Context): Int {
            var statusHeight = -1
            try {
                val clazz = Class.forName("com.android.internal.R\$dimen")
                val `object` = clazz.newInstance()
                val height = clazz.getField("status_bar_height")[`object`].toString().toInt()
                statusHeight = context.resources.getDimensionPixelSize(height)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return statusHeight
        }

        /**
         * 获取屏幕宽度和高度，单位为px
         * @param context
         * @return
         */
        fun getScreenMetrics(context: Context): Point {
            val dm = context.resources.displayMetrics
            val w_screen = dm.widthPixels
            val h_screen = dm.heightPixels
            return Point(w_screen, h_screen)
        }

        /**
         * 获取当前屏幕截图，包含状态栏
         *
         * @param activity
         * @return
         */
        fun snapShotWithStatusBar(activity: Activity): Bitmap? {
            val view = activity.window.decorView
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache()
            val bmp = view.drawingCache
            val width = getScreenWidth(activity)
            val height = getScreenHeight(activity)
            var bp: Bitmap? = null
            bp = Bitmap.createBitmap(bmp, 0, 0, width, height)
            view.destroyDrawingCache()
            return bp
        }

        /**
         * 获取当前屏幕截图，不包含状态栏
         *
         * @param activity
         * @return
         */
        fun snapShotWithoutStatusBar(activity: Activity): Bitmap? {
            val view = activity.window.decorView
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache()
            val bmp = view.drawingCache
            val frame = Rect()
            activity.window.decorView.getWindowVisibleDisplayFrame(frame)
            val statusBarHeight = frame.top
            val width = getScreenWidth(activity)
            val height = getScreenHeight(activity)
            var bp: Bitmap? = null
            bp = Bitmap.createBitmap(
                bmp, 0, statusBarHeight, width, height
                        - statusBarHeight
            )
            view.destroyDrawingCache()
            return bp
        }

        fun ScreenInfo(context: Context) {
            val displayMetrics = context.resources.displayMetrics
            Log.e(
                "eeeee",
                "Density is " + displayMetrics.density + " densityDpi is " + displayMetrics.densityDpi + " height: " + displayMetrics.heightPixels + " width: " + displayMetrics.widthPixels
            )
            Toast.makeText(
                context,
                "Density is " + displayMetrics.density + " densityDpi is " + displayMetrics.densityDpi + " height: " + displayMetrics.heightPixels + " width: " + displayMetrics.widthPixels,
                Toast.LENGTH_LONG
            ).show()
        }

        //获取虚拟按键的高度
        fun getNavigationBarHeight(context: Context): Int {
            var result = 0
            if (hasNavBar(context)) {
                val res = context.resources
                val resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android")
                if (resourceId > 0) {
                    result = res.getDimensionPixelSize(resourceId)
                }
            }
            return result
        }

        /**
         * 检查是否存在虚拟按键栏
         *
         * @param context
         * @return
         */
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        fun hasNavBar(context: Context): Boolean {
            val res = context.resources
            val resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android")
            return if (resourceId != 0) {
                var hasNav = res.getBoolean(resourceId)
                // check override flag
                val sNavBarOverride = navBarOverride
                if ("1" == sNavBarOverride) {
                    hasNav = false
                } else if ("0" == sNavBarOverride) {
                    hasNav = true
                }
                hasNav
            } else { // fallback
                !ViewConfiguration.get(context).hasPermanentMenuKey()
            }
        }

        /**
         * 判断虚拟按键栏是否重写
         *
         * @return
         */
        private val navBarOverride: String?
            private get() {
                var sNavBarOverride: String? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        val c = Class.forName("android.os.SystemProperties")
                        val m = c.getDeclaredMethod("get", String::class.java)
                        m.isAccessible = true
                        sNavBarOverride = m.invoke(null, "qemu.hw.mainkeys") as String
                    } catch (e: Throwable) {
                    }
                }
                return sNavBarOverride
            }

        /**
         * 是否大于6英寸
         *
         * @param ctx
         * @return
         */
        fun isOver6Inch(ctx: Activity): Boolean {
            return if (getScreenPhysicalSize(ctx) >= 6.0) true else false
        }

        /**
         * 获取屏幕尺寸
         *
         * @param ctx
         * @return
         */
        fun getScreenPhysicalSize(ctx: Activity): Double {
            val dm = DisplayMetrics()
            ctx.windowManager.defaultDisplay.getMetrics(dm)
            val diagonalPixels = Math.sqrt(
                Math.pow(
                    dm.widthPixels.toDouble(),
                    2.0
                ) + Math.pow(dm.heightPixels.toDouble(), 2.0)
            )
            return diagonalPixels / (160 * dm.density)
        }

        /**
         * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
         */
        fun dp2px(dpValue: Float, context: Context): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        /**
         * 根据手机的分辨率从 dip(像素) 的单位 转成为 px
         */
//        fun dip2px(pxValue: Float): Int {
//            return dp2px(pxValue)
//        }

        /**
         * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
         */
//        fun dp2px(dpValue: Float): Int {
////        return (int) (dpValue * getScreenDensity() + 0.5f);
//            return TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP,
//                dpValue, MyAppcation.getInstance().getResources().getDisplayMetrics()
//            ).toInt()
//        }

        // 获取指定Activity的截屏，保存到png文件
        fun takeScreenShot(activity: Activity): Bitmap? {

            // View是你需要截图的View
            val view = activity.window.decorView
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache()
            val b1 = view.drawingCache

            // 获取状态栏高度
            val frame = Rect()
            activity.window.decorView.getWindowVisibleDisplayFrame(frame)
            val statusBarHeight = frame.top
            //        System.out.println(statusBarHeight);

            // 获取屏幕长和高
            var width = activity.windowManager.defaultDisplay.width
            var height = b1.height
            val brand = Build.BRAND //获得手机品牌
            var b: Bitmap? = null
            //如果屏幕大于900*500则取900*500，否则按照屏幕大小截图
            if (width > 500 && height < 900) {
                width = 500
                height = 900
            }
            b = if (brand == "Meizu") {
                //            b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - 5 * statusBarHeight
                //            );
                Bitmap.createBitmap(b1, 0, 0, width, height)
            } else {

                // 去掉标题栏
                Bitmap.createBitmap(b1, 0, 0, width, height)
                //            b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
                //                    - statusBarHeight);
            }
            view.destroyDrawingCache()
            return b
        }

        /**
         * 截图全屏
         */
        fun getTotleScreenShot(viewContainer: ViewGroup, vararg views: View): Bitmap {
            val width = viewContainer.width
            var h = 0
            for (i in 0 until viewContainer.childCount) {
                h += viewContainer.getChildAt(i).height
            }
            val screenBitmap = Bitmap.createBitmap(width, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(screenBitmap)
            for (view in views) {
                view.visibility = View.VISIBLE
                view.isDrawingCacheEnabled = true
                canvas.drawBitmap(view.drawingCache, view.left.toFloat(), view.top.toFloat(), null)
            }
            return screenBitmap
        }

        fun getTextHeight(paint: Paint, text: String): Int {
            val rect = Rect()
            paint.getTextBounds(text, 0, text.length, rect)
            return rect.height()
        }
    }
}