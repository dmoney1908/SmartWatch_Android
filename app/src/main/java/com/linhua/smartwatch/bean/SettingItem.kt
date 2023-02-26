package com.linhua.smartwatch.bean

import com.linhua.smartwatch.entity.MultipleEntity

class DeviceItem(itemType: Int, val highlight: Boolean = false) : MultipleEntity(itemType) {
    var name: String? = null
    var status: Boolean = false
    var mac: String? = null
    var icon: String? = null
    var selected: Boolean = false
}