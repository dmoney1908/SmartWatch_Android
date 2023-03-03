package com.linhua.smartwatch.fragment

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.linhua.smartwatch.R
import com.linhua.smartwatch.activity.ScanDeviceReadyActivity
import com.linhua.smartwatch.adapter.DeviceAdapter
import com.linhua.smartwatch.base.BaseFragment
import com.linhua.smartwatch.bean.DeviceItem
import com.linhua.smartwatch.bean.DeviceModel
import com.linhua.smartwatch.entity.MultipleEntity
import com.linhua.smartwatch.utils.*
import com.lxj.xpopup.XPopup

class DeviceFragment: BaseFragment(){
    var hostView: View? = null
    var deviceItemList = mutableListOf<DeviceItem>()
    private val deviceAdapter = DeviceAdapter(mutableListOf()).apply {
        setOnItemChildClickListener(object : OnMultiChildClickListener() {
            override fun onSingleClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                when (view.id) {
                    R.id.ib_delete -> {
                        XPopup.Builder(requireContext())
                            .asConfirm("", "Are you sure to delete this watch？") {
                                DeviceManager.removeDevice(position)
                                reloadData()
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
        val rvDevices = hostView?.findViewById<RecyclerView>(R.id.rv_devices)
        if (rvDevices != null) {
            rvDevices.adapter = deviceAdapter
            rvDevices.layoutManager = LinearLayoutManager(this.context)
        }
        hostView?.findViewById<ImageView>(R.id.iv_add)?.setOnClickListener {
            this.context?.let { it ->
                IntentUtil.goToActivity(
                    it,
                    ScanDeviceReadyActivity::class.java
                )
            }
        }
        return hostView
    }

    override fun initData() {

    }
    override fun onListener() {
    }

    override fun onResume() {
        super.onResume()
        reloadData()
    }

    fun reloadData() {
        convertDeviceItems()
        deviceAdapter.setNewInstance(deviceItemList)
        deviceAdapter.notifyDataSetChanged()
    }

    private fun convertDeviceItems() {
        deviceItemList.clear()
        for (bean in DeviceManager.getDeviceList()) {
            bean.let {
                deviceItemList.add(DeviceItem(MultipleEntity.TWO).apply {
                    name = bean.mDeviceName
                    mac = bean.mDeviceAddress
                    status = bean.equals(DeviceManager.getCurrentDevice())
                })
            }
        }
    }

}