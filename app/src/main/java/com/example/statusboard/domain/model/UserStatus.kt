package com.example.statusboard.domain.model

enum class UserStatus(val label: String) {
    FREE("Free"),
    DND("Do Not Disturb"),
    AWAY("Away"),
    SLEEPING("Sleeping");

    companion object {
        fun fromString(value: String?): UserStatus =
            entries.firstOrNull { it.name == value } ?: FREE
    }
}