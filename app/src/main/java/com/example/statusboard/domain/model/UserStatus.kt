package com.example.statusboard.domain.model

enum class UserStatus(val label: String){
    FREE("Free"),
    BUSY("Busy"),
    AWAY("Away"),
    OFFLINE("Offline")

    companion object {
        fun fromstring (value: String?): UserStatus =
            entries.firstOrNull { it.label == value } ?: FREE
    }
}