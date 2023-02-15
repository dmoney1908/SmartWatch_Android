package com.linhua.smartwatch

import com.linhua.smartwatch.base.BaseActivity
import com.linhua.smartwatch.utils.FragmentManage
import com.roughike.bottombar.BottomBar

class MainActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onListener() {
        val bottom_bar = findViewById<BottomBar>(R.id.bottom_bar)
        bottom_bar.setOnTabSelectListener{
            val transAction=supportFragmentManager.beginTransaction()
            transAction.replace(R.id.content_container,
                FragmentManage.fragmentManage.getFragmentById(it)!!,it.toString())
            transAction.commit()
        }
    }
}