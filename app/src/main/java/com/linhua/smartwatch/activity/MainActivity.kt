package com.linhua.smartwatch.activity

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseActivity

class MainActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onListener() {
        val bottomView = findViewById<BottomNavigationView>(R.id.bottom_view)
//        bottom_bar.setOnTabSelectListener{
//            val transAction=supportFragmentManager.beginTransaction()
//            transAction.replace(R.id.content_container,
//                FragmentManage.fragmentManage.getFragmentById(it)!!,it.toString())
//            transAction.commit()
//        }
    }
}