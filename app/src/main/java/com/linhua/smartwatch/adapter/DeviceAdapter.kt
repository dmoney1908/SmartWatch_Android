package com.linhua.smartwatch.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.opengl.Visibility
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linhua.smartwatch.R
import com.linhua.smartwatch.bean.DeviceItem
import com.linhua.smartwatch.entity.MultipleEntity
import com.blankj.utilcode.util.ColorUtils

class DeviceAdapter(data: MutableList<DeviceItem>?) :
    BaseMultiItemQuickAdapter<DeviceItem, BaseViewHolder>(data) {
    init {
        addItemType(MultipleEntity.TWO, R.layout.item_device_info)
        addChildClickViewIds(R.id.ib_delete)
        addChildClickViewIds(R.id.ib_reconnect)
    }

    @SuppressLint("ResourceType")
    override fun convert(holder: BaseViewHolder, item: DeviceItem) {
        when (holder.itemViewType) {
            MultipleEntity.TWO -> {
                holder.setText(R.id.tv_title, item.name)
                holder.setText(R.id.tv_mac, item.mac)
                holder.getView<ImageView>(R.id.iv_avatar).setImageResource(R.drawable.main_default_avatar)
                var drawable = holder.getView<TextView>(R.id.tv_status).background as GradientDrawable
                if (item.status) {
                    val c = context.resources.getColor(R.color.light_green, null)
                    drawable.setColor(ColorUtils.getColor(R.color.light_green))
                    holder.setTextColor(R.id.tv_status, ColorUtils.getColor(R.color.green))
                    holder.getView<TextView>(R.id.ib_reconnect).visibility = View.INVISIBLE
                } else {
                    drawable.setColor(ColorUtils.getColor(R.color.light_red))
                    holder.setTextColor(R.id.tv_status, ColorUtils.getColor(R.color.red))
                    holder.getView<TextView>(R.id.ib_reconnect).visibility = View.VISIBLE
                    holder.getView<TextView>(R.id.ib_reconnect).text = context.resources.getString(R.string.reconnect)
                }
            }
        }
    }
}