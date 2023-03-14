package com.linhua.smartwatch.bean

import android.graphics.Bitmap
import android.media.Image
import com.linhua.smartwatch.entity.MultipleEntity

class UserItem(itemType: Int) : MultipleEntity(itemType) {
    var name = ""
    var detail  = ""
    var avatar: Bitmap? = null
}