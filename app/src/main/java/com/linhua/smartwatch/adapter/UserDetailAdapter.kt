package com.linhua.smartwatch.adapter

import android.annotation.SuppressLint
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.linhua.smartwatch.R
import com.linhua.smartwatch.bean.UserItem
import com.linhua.smartwatch.entity.MultipleEntity

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
            }
        }
    }
}