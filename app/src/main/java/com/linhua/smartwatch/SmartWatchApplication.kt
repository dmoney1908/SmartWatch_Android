package com.linhua.smartwatch

import android.app.Application
import android.content.Context

class SmartWatchApplication: Application() {
    companion object {
        lateinit var instance: SmartWatchApplication
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    fun getInstance(): SmartWatchApplication? {
        return instance
    }
}