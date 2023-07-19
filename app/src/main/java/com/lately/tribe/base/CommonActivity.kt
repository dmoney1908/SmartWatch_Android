package com.lately.tribe.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gyf.immersionbar.ImmersionBar
import com.lately.tribe.R
import com.lately.tribe.utils.DialogHelperNew
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil

public abstract class CommonActivity:AppCompatActivity() {
    protected val TAG: String = this.javaClass.simpleName
    private var mImmersionBar: ImmersionBar? = null

    protected open fun showToast(content: String?) {
        runOnUiThread {
            ToastUtil.showToast(
                this,
                content
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stupStatusBar()
    }

    private fun stupStatusBar() {
        // 初始化沉浸式状态栏
        if (isStatusBarEnabled()) {
            getStatusBarConfig().init()
        }
    }

    /**
     * 是否使用沉浸式状态栏
     */
    protected open fun isStatusBarEnabled(): Boolean {
        return true
    }

    /**
     * 状态栏字体深色模式
     */
    protected open fun isStatusBarDarkFont(): Boolean {
        return true
    }

    /**
     * 获取状态栏沉浸的配置对象
     */
    open fun getStatusBarConfig(): ImmersionBar {
        if (mImmersionBar == null) {
            mImmersionBar = createStatusBarConfig()
        }
        return mImmersionBar as ImmersionBar
    }

    /**
     * 初始化沉浸式状态栏
     */
    protected open fun createStatusBarConfig(): ImmersionBar {
        return ImmersionBar.with(this)
            .statusBarColor(R.color.white)// 默认状态栏字体颜色为黑色
//            .statusBarView(view)  //解决状态栏和布局重叠问题，任选其一
            .fitsSystemWindows(true)
            .statusBarDarkFont(isStatusBarDarkFont()) // 指定导航栏背景颜色
            .navigationBarColor(R.color.white) // 状态栏字体和导航栏内容自动变色，必须指定状态栏颜色和导航栏颜色才可以自动变色
            .autoDarkModeEnable(false, 0.2f)
    }

    open fun showLoading() {
        runOnUiThread {
            DialogHelperNew.buildWaitDialog(this, true)
        }
    }

    open fun hideLoading() {
        runOnUiThread {
            DialogHelperNew.dismissWait()
        }
    }
}