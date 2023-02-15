package com.linhua.smartwatch

import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.linhua.smartwatch.base.BaseActivity
import com.linhua.smartwatch.utils.FragmentManage

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