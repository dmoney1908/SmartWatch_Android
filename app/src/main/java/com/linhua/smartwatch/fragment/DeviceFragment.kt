package com.linhua.smartwatch.fragment

import android.view.View
import android.widget.TextView
import com.linhua.smartwatch.base.BaseFragment

class DeviceFragment: BaseFragment(){
    override fun initView(): View? {
        var textView: TextView = TextView(activity)
        textView.text="Device"
        return textView
    }
}