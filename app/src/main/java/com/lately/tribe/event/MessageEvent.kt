package com.lately.tribe.event

class MessageEvent(val type: String) {
    companion object {
        const val DeviceStatusChanged = "DeviceConnectionStatusChanged"
        const val UnitChanged = "UnitChanged"
    }
}