package com.example.statusboard.data

import com.example.statusboard.domain.model.UserProfile
import com.example.statusboard.domain.model.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val usersCollection = Firebase.firestore.collection("users")

    // Listen to current user profile
    fun observeCurrentUser(): Flow<UserProfile?> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = usersCollection
            .document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                } else {
                    trySend(snapshot?.toUserProfile())
                }
            }

        awaitClose { listener.remove() }
    }

    // Listen to this user's friends list
    // Firestore path: users/{uid}/friends/{friendDoc}
    fun observeFriends(): Flow<List<UserProfile>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = usersCollection
            .document(user.uid)
            .collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                } else {
                    val list = snapshot?.documents
                        ?.mapNotNull { it.toFriendProfile() }
                        ?: emptyList()
                    trySend(list)
                }
            }

        awaitClose { listener.remove() }
    }

    // Update only the status field for current user
    fun updateStatus(status: UserStatus) {
        val user = auth.currentUser ?: return

        usersCollection.document(user.uid)
            .update(
                mapOf(
                    "status" to status.name,
                    "lastUpdated" to System.currentTimeMillis()
                )
            )
    }
}

// Map Firestore doc -> current user profile
private fun DocumentSnapshot.toUserProfile(): UserProfile? {
    val nickname = getString("nickname") ?: return null
    val statusString = getString("status") ?: "FREE"

    val status = runCatching { UserStatus.valueOf(statusString) }
        .getOrDefault(UserStatus.FREE)

    return UserProfile(
        name = nickname,
        status = status
    )
}

// Map friend document -> UserProfile
// expected fields: nickname, status
private fun DocumentSnapshot.toFriendProfile(): UserProfile? {
    val nickname = getString("nickname") ?: return null
    val statusString = getString("status") ?: "FREE"

    val status = runCatching { UserStatus.valueOf(statusString) }
        .getOrDefault(UserStatus.FREE)

    return UserProfile(
        name = nickname,
        status = status
    )
}