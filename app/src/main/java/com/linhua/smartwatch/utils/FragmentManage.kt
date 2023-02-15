package com.linhua.smartwatch.utils

import androidx.fragment.app.Fragment
import com.linhua.smartwatch.R
import com.linhua.smartwatch.base.BaseFragment
import com.linhua.smartwatch.fragment.HomeFragment
import com.linhua.smartwatch.fragment.SportFragment
import com.linhua.smartwatch.fragment.PersonalFragment
import com.linhua.smartwatch.fragment.DeviceFragment

class FragmentManage private constructor(){
    //创建单例模式，私有化构造函数，懒加载方式定义变量，创建联合对象
    val homeFragment by lazy{HomeFragment()}
    val sportFragment by lazy{SportFragment()}
    val deviceFragment by lazy{DeviceFragment()}
    val personalFragment by lazy{PersonalFragment()}

    companion object{
        val fragmentManage by lazy{
            FragmentManage()
        }
    }

    fun getFragmentById(id:Int):BaseFragment?{
        when(id){
            R.id.navigation_home->return homeFragment
            R.id.navigation_sport->return sportFragment
            R.id.navigation_device->return deviceFragment
            R.id.navigation_profile->return personalFragment
        }
        return null
    }
}