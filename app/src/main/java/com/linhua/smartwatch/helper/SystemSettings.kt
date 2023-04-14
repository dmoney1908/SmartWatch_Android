package com.linhua.smartwatch.helper

import com.google.gson.Gson

data class SystemSettings(var unitSettings: Int = 0, var temprUnit: Int = 0) {
    fun deepCopy(): SystemSettings {
        return Gson().fromJson(Gson().toJson(this), this.javaClass)
    }
}

