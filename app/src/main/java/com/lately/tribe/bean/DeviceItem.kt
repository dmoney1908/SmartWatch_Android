package com.lately.tribe.bean

import com.lately.tribe.entity.MultipleEntity

class DeviceItem(itemType: Int, val highlight: Boolean = false) : MultipleEntity(itemType) {
    var name: String? = null
    var status: Boolean = false
    var mac: String? = null
    var icon: String? = null
    var selected: Boolean = false
}