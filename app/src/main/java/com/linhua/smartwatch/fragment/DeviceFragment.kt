package com.linhua.smartwatch.fragment

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.linhua.smartwatch.R
import com.linhua.smartwatch.adapter.DeviceAdapter
import com.linhua.smartwatch.base.BaseFragment
import com.linhua.smartwatch.bean.DeviceModel
import com.linhua.smartwatch.utils.OnMultiChildClickListener
import com.linhua.smartwatch.utils.OnMultiClickListener
import com.lxj.xpopup.XPopup

class DeviceFragment: BaseFragment(){
    var hostView: View? = null
    var deviceList = mutableListOf<DeviceModel>()
    private val deviceAdapter = DeviceAdapter(mutableListOf()).apply {
        setOnItemClickListener(object : OnMultiClickListener() {
            override fun onSingleClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
            }
        })
        setOnItemChildClickListener(object : OnMultiChildClickListener() {
            override fun onSingleClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                when (view.id) {
                    R.id.ib_delete -> {
                        XPopup.Builder(requireContext())
                            .asConfirm("Are you sure to delete this watch？", "") {

                            }.show()
                    }
                    R.id.ib_reconnect -> {
                        val item = data[position]
                    }
                }
            }
        })

    }
    override fun initView(): View? {
        hostView = View.inflate(activity, R.layout.fragment_devices,null) as View?
        return hostView
    }

    override fun initData() {
        val rvDevices = hostView?.findViewById<RecyclerView>(R.id.rv_devices)
        if (rvDevices != null) {
            rvDevices.adapter = deviceAdapter
        }
    }
    override fun onListener() {
    }
}