package com.linhua.smartwatch.fragment

import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.linhua.smartwatch.R
import com.linhua.smartwatch.activity.ScanDeviceReadyActivity
import com.linhua.smartwatch.adapter.DeviceAdapter
import com.linhua.smartwatch.base.BaseFragment
import com.linhua.smartwatch.bean.DeviceItem
import com.linhua.smartwatch.entity.MultipleEntity
import com.linhua.smartwatch.utils.*
import com.lxj.xpopup.XPopup
import com.zhj.bluetooth.zhjbluetoothsdk.bean.BLEDevice
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.BluetoothLe
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeConnectListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.ConnBleException
import com.zhj.bluetooth.zhjbluetoothsdk.util.ToastUtil.showToast

class DeviceFragment: BaseFragment(){
    var hostView: View? = null
    protected val TAG: String = this.javaClass.simpleName

    private var isConnecting = false
    private var connectDevice: BLEDevice? = null
    var deviceItemList = mutableListOf<DeviceItem>()
    var mBluetoothLe:BluetoothLe? = null
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
                        if (isConnecting)return
                        if (!DeviceManager.isSDKAvailable) {
                            showToast(activity, resources.getString(R.string.sdk_not_available))
                            return
                        }
                        addConnectionListener()
                        val item = data[position]
                        connectDevice = DeviceManager.getDeviceList()[position]
                        mBluetoothLe!!.startConnect(item.mac)
                    }
                }
            }
        })

    }


    override fun initView(): View? {
        mBluetoothLe = BluetoothLe.getDefault()
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

        if (!mBluetoothLe!!.isBluetoothOpen) {
            showToast(activity, resources.getString(R.string.sdk_not_available))
        } else if (!CommonUtil.isOPen(requireActivity())) {
            showToast(activity, resources.getString(R.string.sdk_not_available))
        }
        return hostView
    }

    private fun addConnectionListener() {
        mBluetoothLe!!.setOnConnectListener(TAG, object : OnLeConnectListener() {
            override fun onDeviceConnecting() {}
            override fun onDeviceConnected() {}
            override fun onDeviceDisconnected() {
                isConnecting = false
                connectDevice = null
            }

            override fun onServicesDiscovered(bluetoothGatt: BluetoothGatt) {
                isConnecting = false
                if (connectDevice != null) {
                    DeviceManager.setConnectedDevice(connectDevice)
                    DeviceManager.addDevice(connectDevice!!)
                    reloadData()
                    mBluetoothLe!!.destroy(TAG)
                }
            }

            override fun onDeviceConnectFail(e: ConnBleException) {
                isConnecting = false
                connectDevice = null
                mBluetoothLe!!.destroy(TAG)
            }
        })
    }

    override fun initData() {

    }
    override fun onListener() {
    }

    override fun onResume() {
        super.onResume()
        reloadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        //根据TAG注销监听，避免内存泄露
        mBluetoothLe!!.destroy(TAG)
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
                    status = bean.equals(DeviceManager.getConnectedDevice())
                })
            }
        }
    }

}