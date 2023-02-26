package com.linhua.smartwatch.utils

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener

/**
 * @ProjectName : dayulong
 * @Author : QI.JIA
 * @Time : 2021/4/29 13:50
 * @Description : 文件描述
 */
abstract class OnMultiChildClickListener: OnItemChildClickListener {
    private var clickTime = System.currentTimeMillis()
    override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (System.currentTimeMillis() - clickTime > 666) {
            clickTime = System.currentTimeMillis()
            onSingleClick(adapter, view, position)
        }
    }

    abstract fun onSingleClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int)
}