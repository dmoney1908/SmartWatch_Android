package com.linhua.smartwatch.helper

import android.graphics.Bitmap
import com.google.gson.Gson

data class UserInfo(var name: String = "Tribe",
                    var avatar: Bitmap? = null,
                    var signature: String = "Love Sports, Love life~",
                    var email: String = "",
                    var sex: Int = 0,
                    var age: Int = 0,
                    var height: Int = 0,
                    var weight: Int = 0,
                    var birthday: String = "") {
    fun deepCopy(): UserInfo {
        return Gson().fromJson(Gson().toJson(this), this.javaClass)
    }
}
