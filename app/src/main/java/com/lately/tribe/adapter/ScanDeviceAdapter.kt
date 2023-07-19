package com.lately.tribe.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lately.tribe.R
import com.lately.tribe.base.BaseAdapter
import com.lately.tribe.base.BaseViewHolder
import com.zhj.bluetooth.zhjbluetoothsdk.bean.BLEDevice

/**
 * Created by Administrator on 2019/7/10.
 */
class ScanDeviceAdapter(mContext: Context?, mList: MutableList<BLEDevice?>?) :
    BaseAdapter<BLEDevice?, ScanDeviceAdapter.ViewHolder?>(
        mContext,
        mList
    ) {
    fun setData(mList: MutableList<BLEDevice?>) {
        this.mList = mList
        notifyDataSetChanged()
    }

    override fun onNormalBindViewHolder(
        holder: ViewHolder?,
        itemBean: BLEDevice?,
        position: Int
    ) {
        if (position == connPosition) {
            holder?.tvState?.visibility = View.GONE
            holder?.tvConnect?.setVisibility(View.VISIBLE)
            val animation = AnimationUtils.loadAnimation(mContext, R.anim.progress_drawable)
            holder?.tvConnect?.startAnimation(animation) //開始动画
        } else {
            holder?.tvConnect?.setVisibility(View.GONE)
            holder?.tvState?.setVisibility(View.VISIBLE)
            if (itemBean != null) {
                if (Math.abs(itemBean.mRssi) <= 70) {
                    holder?.tvState?.setImageResource(R.mipmap.device_rssi_1)
                } else if (Math.abs(itemBean.mRssi) <= 90) {
                    holder?.tvState?.setImageResource(R.mipmap.device_rssi_2)
                } else {
                    holder?.tvState?.setImageResource(R.mipmap.device_rssi_3)
                }
            }
        }
        holder?.tvDeviceName?.setText(itemBean?.mDeviceName)
        holder?.tvMac?.setText(itemBean?.mDeviceAddress)
        holder?.layoutItem?.setOnClickListener(View.OnClickListener { v: View? ->
            mOnItemClickListener?.onItemClick(
                holder.layoutItem,
                position
            )
        })
    }

    protected override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        val view: View = inflater.inflate(R.layout.item_scan_device, parent, false)
        return ViewHolder(view)
    }

    private var connPosition = -1
    fun connecting(position: Int) {
        connPosition = position
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View?) : BaseViewHolder(itemView) {
        var tvDeviceName: TextView? = itemView?.findViewById(R.id.tvDeviceName)
        var tvMac: TextView? = itemView?.findViewById(R.id.tvMac)

        var layoutItem: RelativeLayout? = itemView?.findViewById(R.id.layoutItem)

        var tvState: ImageView? = itemView?.findViewById(R.id.tvState)

        var tvConnect: ImageView? = itemView?.findViewById(R.id.tvConnect)
    }

}