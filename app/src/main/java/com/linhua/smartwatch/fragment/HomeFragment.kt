package com.linhua.smartwatch.fragment

import android.graphics.Color
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseFragment
import java.util.logging.Handler

class HomeFragment: BaseFragment(){
    override fun initView(): View? {
        return View.inflate(activity, R.layout.fragment_home,null)
    }

    override fun initData() {
    }
    override fun onListener() {
    }
}