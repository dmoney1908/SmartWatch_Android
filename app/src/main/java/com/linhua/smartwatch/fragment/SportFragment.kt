package com.linhua.smartwatch.fragment

import android.view.View
import android.widget.TextView
import com.linhua.smartwatch.base.BaseFragment

class SportFragment: BaseFragment(){
    override fun initView(): View? {
        var textView: TextView = TextView(activity)
        textView.text="Sport"
        return textView
    }
}