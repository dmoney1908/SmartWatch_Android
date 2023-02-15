package com.linhua.smartwatch.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        initData()
        onListener()
    }

    protected  open fun onListener() {
    }

    protected open fun initData() {
    }

    abstract fun getLayoutId(): Int
}