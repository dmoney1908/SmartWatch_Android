package com.lately.tribe.utils

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener

/**
 * @ProjectName : LargeLong
 * @Author : QI.JIA
 * @Time : 2021/1/6 13:55
 * @Description : 文件描述
 */
abstract class OnMultiClickListener : OnItemClickListener {
    private var clickTime = System.currentTimeMillis()

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (System.currentTimeMillis() - clickTime > 666) {
            clickTime = System.currentTimeMillis()
            onSingleClick(adapter, view, position)
        }
    }

    abstract fun onSingleClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int)
}