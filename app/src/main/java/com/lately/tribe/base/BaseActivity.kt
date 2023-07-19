package com.lately.tribe.base

import android.os.Bundle
import com.lately.tribe.utils.PermissionUtil

public abstract class BaseActivity: CommonActivity(), PermissionUtil.RequsetResult {
    override fun onCreate(savedInstanceState: Bundle?) {
        prepareData()
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        initData()
        onListener()
    }

    protected  open fun prepareData() {
    }

    protected  open fun onListener() {
    }

    protected open fun initData() {
        permissionUtil = PermissionUtil()
        permissionUtil!!.setRequsetResult(this)
    }

    private var permissionUtil: PermissionUtil? = null

    /**
     * 检测权限，如果返回true,有权限 false 无权限
     * @param permissions 权限
     * @return 是否有权限
     */
    open fun checkSelfPermission(vararg permissions: String?): Boolean {
        return PermissionUtil.checkSelfPermission(*permissions)
    }

    open fun requestPermissions(requestCode: Int, vararg permissions: String?) {
        PermissionUtil.requestPermissions(this, requestCode, *permissions)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtil!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun requestPermissionsSuccess(requestCode: Int) {}
    override fun requestPermissionsFail(requestCode: Int) {}

    abstract fun getLayoutId(): Int
}