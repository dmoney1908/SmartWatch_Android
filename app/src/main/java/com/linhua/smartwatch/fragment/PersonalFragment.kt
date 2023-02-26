package com.linhua.smartwatch.fragment

import android.view.View
import android.widget.TextView
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseFragment

class PersonalFragment: BaseFragment(){
    override fun initView(): View? {
        return View.inflate(activity, R.layout.fragment_mine,null)
    }

    override fun initData() {
    }
    override fun onListener() {
    }
}