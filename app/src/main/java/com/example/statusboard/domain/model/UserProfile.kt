package com.example.statusboard.domain.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val nickname: String = "",
    val photoUrl: String? = null,
    val status: UserStatus = UserStatus.FREE,
    val lastUpdated: Long = System.currentTimeMillis(),
    val avatarIndex: Int = 0,
    val tag: Int=0,
    val autoStatus: Boolean = false,
    val statusExpiresAt: Long? = null,
    val calendarAutoStatusEnabled: Boolean = false,
    val calendarSelectedIds: List<String> = emptyList()
) {
    val handle: String
        get() = if (name.isBlank()) "#$tag" else "$name#$tag"

}