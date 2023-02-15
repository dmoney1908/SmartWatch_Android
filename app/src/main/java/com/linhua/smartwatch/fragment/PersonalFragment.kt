package com.linhua.smartwatch.fragment

import android.view.View
import android.widget.TextView
import com.linhua.smartwatch.base.BaseFragment

class PersonalFragment: BaseFragment(){
    override fun initView(): View? {
        var textView:TextView=TextView(activity)
        textView.text="Mine"
        return textView
    }
}