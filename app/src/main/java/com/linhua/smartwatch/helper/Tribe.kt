package com.linhua.smartwatch.helper

data class Tribe(
    var tribeInfo: TribeInfo? = null,
    var tribeDetail: TribeDetail? = null)

data class TribeInfo (
    val role: Int = 0,
    val code: String = "",
    val name: String = "",
    val avatar: String = ""
)

data class TribeDetail (
    val name: String = "",
    val avatar: String = ""
)

data class TribeMember (
    val name: String = "",
    val email: String = "",
    val avatar: String = "",
    val steps: Int = 0,
    val sleep: Int = 0,
    val role: Int = 0,
    val time: String = ""
)
