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
            R.id.tab_home->return homeFragment
            R.id.tab_favorites->return sportFragment
            R.id.tab_personal->return deviceFragment
            R.id.tab_message->return personalFragment
        }
        return null
    }
}