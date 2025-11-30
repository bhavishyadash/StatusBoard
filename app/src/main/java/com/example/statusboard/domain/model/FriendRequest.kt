package com.example.statusboard.domain.model

data class FriendRequest(
    val id: String = "",
    val fromUid: String = "",
    val fromNickname: String = "",
    val toUid: String = "",
    val toNickname: String = "",
    val status: FriendRequestStatus = FriendRequestStatus.PENDING,
    val createdAt: Long = 0L
)