package com.example.statusboard.domain.model

import com.google.firebase.Timestamp

enum class NotificationType {
    FRIEND_REQUEST,
    FRIEND_ACCEPTED,
    FRIEND_REJECTED
    // Later: MESSAGE, STATUS_UPDATE, etc.
}

data class NotificationItem(
    val id: String = "",
    val type: NotificationType = NotificationType.FRIEND_REQUEST,
    val fromUserId: String = "",
    val fromName: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)