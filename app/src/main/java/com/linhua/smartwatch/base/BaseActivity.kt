package com.linhua.smartwatch.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.linhua.smartwatch.utils.PermissionUtil
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil

public abstract class BaseActivity:AppCompatActivity() {
    protected val TAG: String = this.javaClass.simpleName
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

    protected open fun showToast(content: String?) {
        runOnUiThread {
            ToastUtil.showToast(
                this,
                content
            )
        }
    }

    private val permissionUtil: PermissionUtil? = null

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

    open fun requestPermissionsSuccess(requestCode: Int) {}
    open fun requestPermissionsFail(requestCode: Int) {}

    abstract fun getLayoutId(): Int
}