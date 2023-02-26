package com.linhua.smartwatch.fragment

import android.view.View
import android.widget.TextView
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseFragment

class SportFragment: BaseFragment(){
    override fun initView(): View? {
        return View.inflate(activity, R.layout.fragment_sport,null)
    }

    override fun initData() {
    }
    override fun onListener() {
    }
}