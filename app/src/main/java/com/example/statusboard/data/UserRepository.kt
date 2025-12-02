package com.example.statusboard.data

import com.example.statusboard.domain.model.NotificationType
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object UserRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val users get() = firestore.collection("users")

    private val friendRequests get() = firestore.collection("friend_requests")

    fun sendFriendRequest(
        toUserId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val fromUser = auth.currentUser ?: run {
            onResult(false, "Not logged in")
            return
        }

        // avoid sending to self
        if (toUserId == fromUser.uid) {
            onResult(false, "You cannot add yourself")
            return
        }

        val reqData = hashMapOf(
            "fromUserId" to fromUser.uid,
            "toUserId" to toUserId,
            "status" to "PENDING",
            "createdAt" to Timestamp.now()
        )

        friendRequests.add(reqData)
            .addOnSuccessListener {
                createNotificationForUser(
                    userId = toUserId,
                    type = NotificationType.FRIEND_REQUEST,
                    fromUserId = fromUser.uid,
                    fromName = fromUser.displayName ?: "",
                    message = "sent you a friend request"
                )
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
            }
    }

    fun respondToFriendRequest(
        requestId: String,
        accept: Boolean,
        onResult: (Boolean, String?) -> Unit
    ) {
        val current = auth.currentUser ?: run {
            onResult(false, "Not logged in")
            return
        }

        val docRef = friendRequests.document(requestId)

        firestore.runTransaction { tx ->
            val snapshot = tx.get(docRef)
            if (!snapshot.exists()) return@runTransaction

            val fromUserId = snapshot.getString("fromUserId") ?: return@runTransaction
            val toUserId = snapshot.getString("toUserId") ?: return@runTransaction

            // Only the recipient can respond
            if (toUserId != current.uid) return@runTransaction

            val newStatus = if (accept) "ACCEPTED" else "REJECTED"
            tx.update(docRef, "status", newStatus)

            if (accept) {
                // Add friend both ways: users/{uid}/friends/{friendUid}
                val meFriends = users.document(current.uid).collection("friends")
                val otherFriends = users.document(fromUserId).collection("friends")

                meFriends.document(fromUserId).set(
                    mapOf(
                        "uid" to fromUserId,
                        "createdAt" to Timestamp.now()
                    )
                )
                otherFriends.document(current.uid).set(
                    mapOf(
                        "uid" to current.uid,
                        "createdAt" to Timestamp.now()
                    )
                )
            }

            val type = if (accept) {
                NotificationType.FRIEND_ACCEPTED
            } else {
                NotificationType.FRIEND_REJECTED
            }

            createNotificationForUser(
                userId = fromUserId,
                type = type,
                fromUserId = current.uid,
                fromName = current.displayName ?: "",
                message = if (accept) {
                    "accepted your friend request"
                } else {
                    "rejected your friend request"
                }
            )
        }.addOnSuccessListener {
            onResult(true, null)
        }.addOnFailureListener { e ->
            onResult(false, e.localizedMessage)
        }
    }

    private fun createNotificationForUser(
        userId: String,
        type: NotificationType,
        fromUserId: String,
        fromName: String,
        message: String
    ) {
        val notifRef = users.document(userId).collection("notifications")

        val data = hashMapOf(
            "type" to type.name,
            "fromUserId" to fromUserId,
            "fromName" to fromName,
            "message" to message,
            "isRead" to false,
            "createdAt" to Timestamp.now()
        )

        notifRef.add(data)
    }
}