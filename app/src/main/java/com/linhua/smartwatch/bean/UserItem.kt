package com.linhua.smartwatch.bean

import android.media.Image
import com.linhua.smartwatch.entity.MultipleEntity

class UserItem(itemType: Int) : MultipleEntity(itemType) {
    var name = ""
    var detail  = ""
    var avatar: Image? = null
}