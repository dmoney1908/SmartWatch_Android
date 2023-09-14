package com.lately.tribe.tribe.adapter

import android.annotation.SuppressLint
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lately.tribe.R
import com.lately.tribe.entity.MultipleEntity

class TribeMemberAdapter(data: MutableList<TribeMemberItem>?) :
    BaseMultiItemQuickAdapter<TribeMemberItem, BaseViewHolder>(data) {
    init {
        addItemType(MultipleEntity.ONE, R.layout.item_member_info)
    }

    @SuppressLint("ResourceType")
    override fun convert(holder: BaseViewHolder, item: TribeMemberItem) {
        when (holder.itemViewType) {
            MultipleEntity.ONE -> {
                if (item.name.isNotEmpty()) {
                    holder.setText(R.id.tv_name, item.name)
                } else {
                    holder.setText(R.id.tv_name, item.email)
                }
                holder.setText(R.id.tv_time, item.time)
                holder.getView<ImageView>(R.id.iv_avatar).clipToOutline = true
                if (item.avatar.isNotEmpty()) {
                    Glide.with(context).load(item.avatar).placeholder(R.drawable.avatar_user).centerCrop()
                        .into(holder.getView<ImageView>(R.id.iv_avatar))
                } else {
                    Glide.with(context).load(R.drawable.avatar_user).centerCrop().into(holder.getView<ImageView>(R.id.iv_avatar))
                }
                val steps = item.steps.toString()
                holder.setText(R.id.tv_steps_num, steps)
                var sleepTime = ""
                if (item.sleep % 60 == 0) {
                    sleepTime = String.format("%dh", item.sleep / 60)
                } else {
                    sleepTime = String.format("%dh %dminitue", item.sleep / 60, item.sleep % 60)
                }
                holder.setText(R.id.tv_sleep_num, sleepTime)
            }
        }
    }
}