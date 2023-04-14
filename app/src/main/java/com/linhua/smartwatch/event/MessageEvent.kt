package com.linhua.smartwatch.event

class MessageEvent(val type: String) {
    companion object {
        const val DeviceStatusChanged = "DeviceConnectionStatusChanged"
        const val UnitChanged = "UnitChanged"
    }
}