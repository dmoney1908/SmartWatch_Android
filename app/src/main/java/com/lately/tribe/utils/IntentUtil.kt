package com.lately.tribe.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
/**
 * 页面跳转intent工具类
 * Created by wzl on 2018/6/17.
 */
object IntentUtil {
    /**
     * 跳转到指定页面
     * author wzl
     * date 2018/6/17 下午5:31
     * @param c
     * 上下文对象
     * @param cls
     * 指定跳转的类
     */
    fun goToActivity(c: Context, cls: Class<*>?) {
        val intent = Intent()
        intent.setClass(c, cls!!)
        c.startActivity(intent)
    }

    /**
     * 携带bundle数据跳转到指定页面
     * author wzl
     * date 2018/6/17 下午5:33
     */
    fun goToActivity(c: Context, cls: Class<*>?, bundle: Bundle?) {
        val intent = Intent()
        intent.setClass(c, cls!!)
        intent.putExtras(bundle!!)
        c.startActivity(intent)
    }

    /**
     * 跳转指定页面并返回
     * author wzl
     * date 2018/6/17 下午5:35
     * @param requstCode
     * 请求码
     */
    fun goToActivityForResult(c: Context, cls: Class<*>?, requstCode: Int) {
        val intent = Intent()
        intent.setClass(c, cls!!)
        (c as Activity).startActivityForResult(intent, requstCode)
    }

    /**
     * 携带bundle数据跳转指定页面并返回
     */
    fun goToActivityForResult(c: Context, cls: Class<*>?, bundle: Bundle?, requstCode: Int) {
        val intent = Intent()
        intent.setClass(c, cls!!)
        intent.putExtras(bundle!!)
        (c as Activity).startActivityForResult(intent, requstCode)
    }

    /**
     * 跳转到指定页面并关闭当前页面
     * author wzl
     * date 2018/6/17 下午5:59
     */
    fun goToActivityAndFinish(c: Context, cls: Class<*>?) {
        val intent = Intent()
        intent.setClass(c, cls!!)
        c.startActivity(intent)
        (c as Activity).finish()
    }

    /**
     * 跳转到指定页面并关闭当前页面
     * author wzl
     * date 2018/6/17 下午5:59
     */
    fun goToActivityAndFinish(c: Context, cls: Class<*>?, bundle: Bundle?) {
        val intent = Intent()
        intent.setClass(c, cls!!)
        intent.putExtras(bundle!!)
        c.startActivity(intent)
        (c as Activity).finish()
    }

    /**
     * 跳转到指定页面
     * author wzl
     * date 2018/6/17 下午6:02
     */
    fun goToActivityAndFinishTop(c: Context, cls: Class<*>?) {
        val intent = Intent()
        intent.setClass(c, cls!!)
        // FLAG_ACTIVITY_CLEAR_TOP 销毁目标Activity和它之上的所有Activity，重新创建目标Activity
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        c.startActivity(intent)
        (c as Activity).finish()
    }

    /**
     * 跳转到指定页面
     * author wzl
     * date 2018/6/17 下午6:02
     */
    fun goToActivityAndFinishTop(c: Context, cls: Class<*>?, bundle: Bundle?) {
        val intent = Intent()
        intent.setClass(c, cls!!)
        intent.putExtras(bundle!!)
        // FLAG_ACTIVITY_CLEAR_TOP 销毁目标Activity和它之上的所有Activity，重新创建目标Activity
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        c.startActivity(intent)
        (c as Activity).finish()
    }

    /**
     * 跳转到指定页面
     * author wzl
     * date 2018/6/17 下午6:02
     */
    fun goToActivityAndFinishSingleTop(c: Context, clz: Class<*>?, bundle: Bundle?) {
        val intent = Intent(c, clz)
        intent.putExtras(bundle!!)
        //当该activity处于task栈顶时，可以复用，直接onNewIntent
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val a = c as Activity
        a.startActivity(intent)
        a.finish()
    }

    /**
     * 跳转到指定页面
     * author wzl
     * date 2018/6/17 下午6:02
     */
    fun goToActivityAndFinishSingleTop(c: Context, clz: Class<*>?) {
        val intent = Intent(c, clz)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val a = c as Activity
        a.startActivity(intent)
        a.finish()
    }
}