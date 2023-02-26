package com.linhua.smartwatch.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.Button
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.color.MaterialColors.getColor
import com.linhua.smartwatch.R
import com.linhua.smartwatch.bean.DeviceItem
import com.linhua.smartwatch.entity.MultipleEntity


class DeviceAdapter(data: MutableList<DeviceItem>?) :
    BaseMultiItemQuickAdapter<DeviceItem, BaseViewHolder>(data) {
    init {
        addItemType(MultipleEntity.TWO, R.layout.item_device_info)
    }

    @SuppressLint("ResourceType")
    override fun convert(holder: BaseViewHolder, item: DeviceItem) {
        when (holder.itemViewType) {
            MultipleEntity.TWO -> {
                holder.setText(R.id.tv_title, item.name)
                holder.setText(R.id.tv_mac, item.mac)
                holder.getView<Button>(R.id.ib_reconnect).visibility =
                    if (item.status) View.VISIBLE else View.GONE
                var drawable = holder.getView<Button>(R.id.tv_status).background as GradientDrawable
                if (item.status) {
                    drawable.setColor(getColor(holder.getView<Button>(R.id.tv_status), R.color.light_green))
                    holder.getView<Button>(R.id.tv_status).setTextColor(R.color.green)
                } else {
                    drawable.setColor(getColor(holder.getView<Button>(R.id.tv_status), R.color.light_red))
                    holder.getView<Button>(R.id.tv_status).setTextColor(R.color.red)
                }
            }
        }

    }
}