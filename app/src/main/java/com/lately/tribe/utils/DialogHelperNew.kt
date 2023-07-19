package com.lately.tribe.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.lately.tribe.R

/**
 * 自定义Dialog。
 */
object DialogHelperNew {
    private var waitDialog: Dialog? = null

    /**
     * 创建等待Dialog
     *
     * @param c
     * 上下文环境
     * @param cancelable
     * Dialog是否可以返回取消
     *
     * @return Dialog
     */
    fun buildWaitDialog(c: Context?, cancelable: Boolean): Dialog? {
        if (waitDialog == null) {
            waitDialog = Dialog(c!!, R.style.theme_dialog)
        }
        waitDialog!!.setContentView(LayoutInflater.from(c).inflate(R.layout.dialog_wait, null))
        waitDialog!!.setCancelable(cancelable)
        waitDialog!!.show()
        return waitDialog
    }

    fun dismissWait() {
        if (waitDialog != null) {
            waitDialog!!.dismiss()
            waitDialog = null
        }
    }

    fun showRemindDialog(
        context: Activity, title: String?, tips: String?,
        sureText: String?, listener: View.OnClickListener,
        canleListener: View.OnClickListener
    ): Dialog {
        val dialog = Dialog(context, R.style.center_dialog)
        val view: View = LayoutInflater.from(context).inflate(R.layout.dialog_remind, null)
        val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = title
        val tvTips = view.findViewById<TextView>(R.id.tvTips)
        tvTips.text = tips
        view.findViewById<View>(R.id.tvCanle).setOnClickListener { v: View? ->
            dialog.dismiss()
            canleListener.onClick(view)
        }
        val tvSure = view.findViewById<TextView>(R.id.tvSure)
        tvSure.text = sureText
        tvSure.setOnClickListener { v: View? ->
            dialog.dismiss()
            listener.onClick(view)
        }
        dialog.setContentView(view)
        dialog.setCancelable(false)
        val dialogWindow = dialog.window
        val lp = dialogWindow!!.attributes
        val d = context.resources.displayMetrics // 获取屏幕宽、高用
        lp.width = (d.widthPixels * 0.8).toInt()
        dialogWindow.attributes = lp
        dialog.show()
        return dialog
    }
}