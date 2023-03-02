package com.linhua.smartwatch.activity

import android.app.Application

class MyAppcation : Application() {
    var isConnected = false

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var instance: MyAppcation? = null
            private set
    }
}