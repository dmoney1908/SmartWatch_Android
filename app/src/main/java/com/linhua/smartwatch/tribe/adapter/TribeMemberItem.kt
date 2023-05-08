package com.linhua.smartwatch.tribe.adapter

import com.linhua.smartwatch.entity.MultipleEntity

class TribeMemberItem(itemType: Int, val highlight: Boolean = false) : MultipleEntity(itemType) {
    var name: String = ""
    var email: String = ""
    var avatar: String = ""
    var steps: Int = 0
    var sleep: Int = 0
    var role: Int = 0
    var time: String = ""
}