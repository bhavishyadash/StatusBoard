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

    // Listen to current user's profile in Firestore
    fun observeCurrentUser(): Flow<UserProfile?> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listenerRegistration = usersCollection
            .document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                } else {
                    trySend(snapshot?.toUserProfile())
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    // Update only the status field
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

// Map Firestore doc -> UserProfile
private fun DocumentSnapshot.toUserProfile(): UserProfile? {
    val nickname = getString("nickname") ?: return null
    val statusString = getString("status") ?: "FREE"

    val status = try {
        UserStatus.valueOf(statusString)
    } catch (_: Exception) {
        UserStatus.FREE
    }

    return UserProfile(
        name = nickname,
        status = status
    )
}