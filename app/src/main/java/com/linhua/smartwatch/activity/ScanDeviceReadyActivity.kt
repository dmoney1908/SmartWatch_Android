package com.linhua.smartwatch.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.linhua.smartwatch.R
import com.linhua.smartwatch.adapter.ScanDeviceAdapter
import com.linhua.smartwatch.base.BaseActivity
import com.linhua.smartwatch.base.BaseAdapter
import com.linhua.smartwatch.utils.*
import com.zhj.bluetooth.zhjbluetoothsdk.bean.BLEDevice
import com.zhj.bluetooth.zhjbluetoothsdk.ble.BleSdkWrapper
import com.zhj.bluetooth.zhjbluetoothsdk.ble.HandlerBleDataResult
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.BluetoothLe
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeConnectListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeScanListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.OnLeWriteCharacteristicListener
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.ConnBleException
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.ScanBleException
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.exception.WriteBleException
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.scanner.ScanRecord
import com.zhj.bluetooth.zhjbluetoothsdk.ble.bluetooth.scanner.ScanResult
import com.zhj.bluetooth.zhjbluetoothsdk.util.SPHelper
import java.util.*

open class ScanDeviceReadyActivity : BaseActivity(), BaseAdapter.OnItemClickListener,
    RecyclerRefreshLayout.SuperRefreshLayoutListener {
    var mRecyclerView: RecyclerView? = null
    var mRefreshLayout: RecyclerRefreshLayout? = null
    private var mAdapter: ScanDeviceAdapter? = null
    private var mBluetoothLe: BluetoothLe? = null
    private val i1 = 0
    private val i2 = 0
    private val i3 = 0
    private val i4 = 0

    override fun getLayoutId() : Int {
        return R.layout.activity_scan_device
    }

    override fun initData() {
        super.initData()
        mRecyclerView = findViewById<RecyclerView>(R.id.refresh_recyclerView)
        mRefreshLayout = findViewById<RecyclerRefreshLayout>(R.id.mRefreshLayout)

//        titleName.setText(getResources().getString(R.string.main_pairing))
        mBluetoothLe = BluetoothLe.getDefault()
        mRefreshLayout!!.setSuperRefreshLayoutListener(this)
        mRefreshLayout!!.setColorSchemeColors(Color.RED, Color.GREEN, Color.CYAN)
        mRefreshLayout!!.setCanLoadMore(false)
        mRecyclerView!!.layoutManager = LinearLayoutManager(this)
        if (!DeviceManager.isSDKAvailable) {
            showToast(resources.getString(R.string.sdk_not_available))
            return
        }
        mBluetoothLe!!.setOnConnectListener(TAG, object : OnLeConnectListener() {
            override fun onDeviceConnecting() {}
            override fun onDeviceConnected() {}
            override fun onDeviceDisconnected() {
                isConnecting = false
                connectDevice = null
                if (mAdapter != null) {
                    mAdapter!!.connecting(-1)
                    showList.clear()
                    mAdapter!!.setData(showList as MutableList<BLEDevice?>)
                }
                scan()
            }

            override fun onServicesDiscovered(bluetoothGatt: BluetoothGatt) {
                isConnecting = false
                DeviceManager.setConnectedDevice(connectDevice)
                DeviceManager.addDevice(connectDevice!!)
//                SPHelper.saveBLEDevice(this@ScanDeviceReadyActivity, connectDevice)
                val intent = Intent()
                setResult(RESULT_OK, intent)
                finish()
                //                Random random = new Random();
//                i1 = random.nextInt(10);
//                i2 = random.nextInt(10);
//                i3 = random.nextInt(10);
//                i4 = random.nextInt(10);
//                BleSdkWrapper.setPairingcode(i1, i2, i3, i4, new OnLeWriteCharacteristicListener() {
//                    @Override
//                    public void onSuccess(HandlerBleDataResult  handlerBleDataResult) {
//                        showPairingDialog();
//                    }
//
//                    @Override
//                    public void onFailed(WriteBleException e) {
//
//                    }
//                } );
            }

            override fun onDeviceConnectFail(e: ConnBleException) {
                isConnecting = false
                connectDevice = null
                if (mAdapter != null) {
                    mAdapter!!.connecting(-1)
                    showList.clear()
                    mAdapter!!.setData(showList as MutableList<BLEDevice?>)
                }
                scan()
            }
        })
        if (!mBluetoothLe!!.isBluetoothOpen) {
            DialogHelperNew.showRemindDialog(this,
                getResources().getString(R.string.permisson_location_title),
                getResources().getString(R.string.permisson_location_tips),
                getResources().getString(R.string.permisson_location_open),
                { view -> mBluetoothLe!!.enableBluetooth(this) }) { view -> this@ScanDeviceReadyActivity.finish() }
        }
        if (!CommonUtil.isOPen(this)) {
            DialogHelperNew.showRemindDialog(this,
                resources.getString(R.string.permisson_location_title),
                getResources().getString(R.string.permisson_location_tips),
                getResources().getString(R.string.permisson_location_open),
                { view ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    startActivityForResult(intent, 1000)
                }) { this@ScanDeviceReadyActivity.finish() }
        } else {
            scanDevice
        }
    }

    private fun showPairingDialog(): Dialog {
        val dialog = Dialog(this, R.style.center_dialog)
        val view: View = LayoutInflater.from(this).inflate(R.layout.dialog_pairing, null)
        val et1 = view.findViewById<EditText>(R.id.et1)
        val et2 = view.findViewById<EditText>(R.id.et2)
        val et3 = view.findViewById<EditText>(R.id.et3)
        val et4 = view.findViewById<EditText>(R.id.et4)
        et1.isFocusable = true
        et1.isFocusableInTouchMode = true
        et1.requestFocus()
        et1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    et2.requestFocus()
                }
            }
        })
        et2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    et3.requestFocus()
                }
            }
        })
        et3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    et4.requestFocus()
                }
            }
        })
        view.findViewById<View>(R.id.tvCanle).setOnClickListener { v: View? ->
            dialog.dismiss()
            //退出输入  解绑
            BleSdkWrapper.exitPairingcode(false, object : OnLeWriteCharacteristicListener() {
                override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                    mAdapter!!.connecting(-1)
                    isConnecting = false
                    BluetoothLe.getDefault().unBind(this@ScanDeviceReadyActivity)
                }

                override fun onFailed(e: WriteBleException) {}
            })
        }
        view.findViewById<View>(R.id.tvSure).setOnClickListener { v: View? ->
            if (TextUtils.isEmpty(et1.text.toString()) || TextUtils.isEmpty(
                    et2.text.toString()
                ) ||
                TextUtils.isEmpty(
                    et3.text.toString()
                ) || TextUtils.isEmpty(et4.text.toString())
            ) {
                showToast(getResources().getString(R.string.scan_device_info_put_complete_code))
            } else {
                if (et1.text.toString().toInt() == i1 && et2.text.toString()
                        .toInt() == i2 && et3.text.toString().toInt() == i3 && et4.text.toString()
                        .toInt() == i4
                ) {
                    dialog.dismiss()
                    BleSdkWrapper.exitPairingcode(true, object : OnLeWriteCharacteristicListener() {
                        override fun onSuccess(handlerBleDataResult: HandlerBleDataResult) {
                            isConnecting = false
                            DeviceManager.setConnectedDevice(connectDevice)
                            SPHelper.saveBLEDevice(this@ScanDeviceReadyActivity, connectDevice)
                            finish()
                        }

                        override fun onFailed(e: WriteBleException) {}
                    })
                } else {
                    showToast(resources.getString(R.string.scan_device_info_put_error))
                    et1.setText("")
                    et2.setText("")
                    et3.setText("")
                    et4.setText("")
                }
            }
        }
        dialog.setContentView(view)
        dialog.setCancelable(false)
        val dialogWindow = dialog.window
        val lp = dialogWindow!!.attributes
        val d: DisplayMetrics = this.resources.displayMetrics // 获取屏幕宽、高用
        lp.width = (d.widthPixels * 0.8).toInt()
        dialogWindow.attributes = lp
        dialog.show()
        return dialog
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            if (!CommonUtil.isOPen(this)) {
                this@ScanDeviceReadyActivity.finish()
            } else {
                scanDevice
            }
        } else if (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE) {
            if (!checkSelfPermission(*permissionsLocation)) {
                requestPermissions(ACCESS_FINE_LOCATION_REQUEST_CODE, *permissionsLocation)
            } else {
                scan()
            }
        }
    }

    private fun scan() {
        mBluetoothLe!!.setScanPeriod(10000)
            .setReportDelay(0) //                .setScanWithDeviceName("DfuTarg")
            //                .setScanWithDeviceAddress("E6:78:9C:CE:7B:80")
            .startScan(this, object : OnLeScanListener() {
                @SuppressLint("MissingPermission")
                override fun onScanResult(
                    bluetoothDevice: BluetoothDevice,
                    rssi: Int,
                    scanRecord: ScanRecord
                ) {
                    val device = BLEDevice()
                    device.mDeviceAddress = bluetoothDevice.address
                    device.mDeviceName = bluetoothDevice.name
                    device.mRssi = rssi
                    device.setmBytes(scanRecord.bytes)
                    device.parcelId = scanRecord.parcelId
                    mRefreshLayout!!.onComplete()
                    if (!showList.contains(device)) {
                        showList.add(device)
                        showList.sort()
                        if (mAdapter == null) {
                            mAdapter = ScanDeviceAdapter(this@ScanDeviceReadyActivity, showList as MutableList<BLEDevice?>)
                            mRecyclerView!!.adapter = mAdapter
                        } else {
                            mAdapter!!.setData(showList as MutableList<BLEDevice?>)
                        }
                        mAdapter!!.setOnItemClickListener(this@ScanDeviceReadyActivity)
                    }
                }

                override fun onBatchScanResults(results: List<ScanResult>) {
                    Log.i(TAG, "扫描到设备：$results")
                }

                override fun onScanCompleted() {
                    mBluetoothLe!!.stopScan()
                    mRefreshLayout!!.onComplete()
                }

                override fun onScanFailed(e: ScanBleException) {
                    Log.e(TAG, "扫描错误：$e")
                    onScanCompleted()
                    mBluetoothLe!!.stopScan()
                }
            })
    }

    private val scanDevice: Unit
        get() {
            mRefreshLayout!!.post {
                mRefreshLayout!!.isRefreshing = true
                showList.clear()
                if (mAdapter != null) {
                    mAdapter!!.setData(showList as MutableList<BLEDevice?>)
                }
                if (checkSelfPermission(*permissionsLocation)) {
                    scan()
                } else {
                    requestPermissions(
                        ACCESS_FINE_LOCATION_REQUEST_CODE,
                        *permissionsLocation
                    )
                }
            }
        }
    private val permissionsLocation = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    private var mDialog: Dialog? = null
    override fun requestPermissionsFail(requestCode: Int) {
        super.requestPermissionsFail(requestCode)
        if (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    permissionsLocation[0]
                )
            ) {
                mDialog = DialogHelperNew.showRemindDialog(this,
                    getResources().getString(R.string.permisson_location_title),
                    getResources().getString(R.string.permisson_location_tips),
                    getResources().getString(R.string.permisson_location_open),
                    { view ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri =
                            Uri.fromParts("package", applicationContext.packageName, null)
                        intent.data = uri
                        startActivityForResult(intent, ACCESS_FINE_LOCATION_REQUEST_CODE)
                    }) { view ->
                    mDialog!!.dismiss()
                    this@ScanDeviceReadyActivity.finish()
                }
            } else {
                this@ScanDeviceReadyActivity.finish()
            }
        }
    }

    override fun requestPermissionsSuccess(requestCode: Int) {
        if (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE) {
            scan()
        }
    }

    private val showList: MutableList<BLEDevice> = ArrayList()
    override fun onPause() {
        super.onPause()
        mRefreshLayout!!.onComplete()
    }

    private var isConnecting = false
    private var connectDevice: BLEDevice? = null
    override fun onItemClick(view: View?, position: Int) {
        //链接设备
        if (!BluetoothLe.getDefault().isBluetoothOpen) {
            showToast(getResources().getString(R.string.scan_device_binding))
        } else {
            if (!isConnecting) {
                mAdapter!!.connecting(position)
                connectDevice = showList[position]
                isConnecting = true
                mBluetoothLe!!.stopScan()
                mBluetoothLe!!.startConnect(showList[position].mDeviceAddress)
            }
        }
    }

    override fun onRefreshing() {
        if (isConnecting || mBluetoothLe!!.scanning) {
//            showToast(getResources().getString(R.string.connect_device_str));
            mRefreshLayout!!.onComplete()
        } else {
            isOpenBle
        }
    }

    private val isOpenBle: Unit
        get() {
            if (!BluetoothLe.getDefault().isBluetoothOpen) {
                mRefreshLayout!!.onComplete()
                val commonDialog: CommonDialog = CommonDialog.Builder(this)
                    .isVertical(false).setTitle(R.string.scan_device_blu_not_open)
                    .setLeftButton(R.string.cancel) { dialog, which -> this@ScanDeviceReadyActivity.finish() }
                    .setMessage(R.string.scan_device_open_set)
                    .setRightButton(R.string.scan_device_set) { dialog, which ->
                        BluetoothLe.getDefault().enableBluetooth(this)
                    }
                    .create()
                commonDialog.show()
            } else {
                scanDevice
            }
        }

    override fun onLoadMore() {}
    override fun onBackPressed() {
        if (isConnecting) {
            showToast(resources.getString(R.string.scan_device_binding))
        } else {
            mBluetoothLe!!.stopScan()
            this.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBluetoothLe!!.stopScan()
        //根据TAG注销监听，避免内存泄露
        mBluetoothLe!!.destroy(TAG)
    }

    companion object {
        protected const val ACCESS_FINE_LOCATION_REQUEST_CODE = 100
    }
}