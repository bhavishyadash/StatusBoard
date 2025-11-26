package com.example.statusboard.domain.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val status: UserStatus = UserStatus.FREE,
    val lastUpdated: Long = System.currentTimeMillis()
)
