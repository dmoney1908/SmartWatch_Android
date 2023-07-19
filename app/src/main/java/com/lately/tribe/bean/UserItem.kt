package com.lately.tribe.bean

import android.graphics.Bitmap
import com.lately.tribe.entity.MultipleEntity

class UserItem(itemType: Int) : MultipleEntity(itemType) {
    var name = ""
    var detail  = ""
    var avatar: Bitmap? = null
}