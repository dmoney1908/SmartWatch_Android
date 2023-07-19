package com.lately.tribe.utils

import androidx.fragment.app.Fragment
import com.lately.tribe.R
import com.lately.tribe.fragment.*

class FragmentManage private constructor(){
    //创建单例模式，私有化构造函数，懒加载方式定义变量，创建联合对象
    val homeFragment by lazy{HomeFragment()}
    val tribeFragment by lazy{TribeFragment()}
    val deviceFragment by lazy{DeviceFragment()}
    val personalFragment by lazy{PersonalFragment()}

    companion object{
        val fragmentManage by lazy{
            FragmentManage()
        }
    }

    fun getFragmentById(id:Int):Fragment?{
        when(id){
            R.id.navigation_home->return homeFragment
            R.id.navigation_tribe->return tribeFragment
            R.id.navigation_device->return deviceFragment
            R.id.navigation_profile->return personalFragment
        }
        return null
    }
}