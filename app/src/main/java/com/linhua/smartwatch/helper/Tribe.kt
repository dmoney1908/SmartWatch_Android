package com.linhua.smartwatch.helper

data class Tribe(
    var tribeInfo: TribeInfo? = null,
    var tribeDetail: TribeDetail? = null)

data class TribeInfo (
    var role: Int = 0,
    var code: String = "",
    var name: String = "",
    var avatar: String = ""
)

data class TribeDetail (
    var name: String = "",
    var avatar: String = "",
    var members: MutableList<TribeMember> = mutableListOf()
) {
     fun addMember(member: TribeMember) {
         val item = members.firstOrNull() {
             it.email == member.email
         }

        if (item == null) {
            members.add(member)
        }
    }

    fun updateMember(member: TribeMember) {
        for (index in members.indices) {
            val item = members[index]
            if (item.email == member.email) {
                members.removeAt(index)
                members.add(index, member)
                break
            }
        }
    }

    fun removeMember(member: TribeMember) {
        members.removeAll {
            it.email == member.email
        }
    }
}

data class TribeMember(
    val name: String = "",
    val email: String = "",
    val avatar: String = "",
    var steps: Int = 0,
    var sleep: Int = 0,
    val role: Int = 0,
    var time: String = ""
)
