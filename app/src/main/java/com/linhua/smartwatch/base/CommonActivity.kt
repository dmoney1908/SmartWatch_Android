package com.linhua.smartwatch.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.linhua.smartwatch.utils.PermissionUtil
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil

public abstract class CommonActivity:AppCompatActivity() {
    protected val TAG: String = this.javaClass.simpleName

    protected open fun showToast(content: String?) {
        runOnUiThread {
            ToastUtil.showToast(
                this,
                content
            )
        }
    }
}