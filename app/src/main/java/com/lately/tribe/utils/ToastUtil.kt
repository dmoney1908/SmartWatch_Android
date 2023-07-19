package com.lately.tribe.utils

import android.content.Context
import android.widget.Toast

class ToastUtil {
    private var mToast: Toast? = null
    private var mLastTime = 0L
    var customToast: Toast? = null
    fun showToast(var0: Context, var1: Int, var2: Int) {
        showToast(var0, var0.resources.getString(var1), var2)
    }

    fun showToast(var0: Context?, var1: String?, var2: Int) {
        if (mToast == null) {
            mToast = Toast.makeText(var0, var1, var2)
        }
        mToast!!.setText(var1)
        mToast!!.duration = var2
        mToast!!.setGravity(17, 0, 0)
        if (System.currentTimeMillis() - mLastTime > 1000L) {
            mToast!!.show()
            mLastTime = System.currentTimeMillis()
        }
    }

    fun showToast(var0: Context?, var1: Int) {
        if (var0 != null) {
            val var2 = var0.getString(var1)
            showToast(var0, var2, 0)
        }
    }

    fun showToast(var0: Context?, var1: String?) {
        if (var0 != null) {
            showToast(var0, var1, 0)
        }
    }
}
