package com.lately.tribe.sleep

class SleepStepModel(
    var x: Int = 0,
    var y:Int = 0,
    var width:Int = 0,
    var height:Int = 0,
    var duration: Int = 0,
    var type: Int = 0,
    var beginTime: Int = 0
) {
    val top get() = y
    val bottom get() = y + height
    val left get() = x
    val right get() = x + width
}

