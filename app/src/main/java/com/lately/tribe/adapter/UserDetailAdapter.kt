package com.lately.tribe.adapter

import android.annotation.SuppressLint
import android.widget.ImageView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lately.tribe.R
import com.lately.tribe.bean.UserItem
import com.lately.tribe.entity.MultipleEntity

class UserDetailAdapter(data: MutableList<UserItem>?) :
    BaseMultiItemQuickAdapter<UserItem, BaseViewHolder>(data) {
    init {
        addItemType(MultipleEntity.ONE, R.layout.item_userdetail_text)
        addItemType(MultipleEntity.TWO, R.layout.item_userdetail_avatar)
    }

    @SuppressLint("ResourceType")
    override fun convert(holder: BaseViewHolder, item: UserItem) {
        when (holder.itemViewType) {
            MultipleEntity.ONE -> {
                holder.setText(R.id.tv_name, item.name)
                holder.setText(R.id.tv_detail, item.detail)

            }
            MultipleEntity.TWO -> {
                holder.setText(R.id.tv_name, item.name)
                holder.getView<ImageView>(R.id.iv_avatar).clipToOutline = true
            }
        }
    }
}