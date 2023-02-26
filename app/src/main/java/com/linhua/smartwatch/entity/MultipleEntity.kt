package com.linhua.smartwatch.entity

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

open class MultipleEntity : MultiItemEntity, Serializable {
    final override var itemType: Int
        private set
    var spanSize: Int = 0
    var type: Int = 0

    constructor(itemType: Int, spanSize: Int, content: String?) {
        this.itemType = itemType
        this.spanSize = spanSize
        this.content = content
    }

    constructor(itemType: Int, spanSize: Int) {
        this.itemType = itemType
        this.spanSize = spanSize
    }

    constructor(itemType: Int) {
        this.itemType = itemType
    }

    var content: String? = null

    companion object {
        const val Category = 1
        const val Clump = 2
        const val ONE = 6
        const val TWO = 9
        const val PACT = 19
        const val THREE = 12
        const val BANNER = 3
        const val Empty = 8
        const val END = 99
    }
}