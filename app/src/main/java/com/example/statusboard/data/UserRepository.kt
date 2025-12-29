package com.example.statusboard.home

import com.example.statusboard.data.usersCollection
import com.example.statusboard.domain.model.UserProfile
import com.example.statusboard.domain.model.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UserRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun setManualStatus(status: UserStatus) {
        val uid = auth.currentUser?.uid ?: return
        usersCollection.document(uid).update(
            mapOf(
                "status" to status.name,
                "autoStatus" to false,
                "statusExpiresAt" to null
            )
        )
    }

    fun setCalendarAutoStatusEnabled(enabled: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        usersCollection.document(uid).update("calendarAutoStatusEnabled", enabled)
    }

    fun listenToCurrentUser(callback: (UserProfile?, Exception?) -> Unit): ListenerRegistration? {
        val user = auth.currentUser ?: run {
            callback(null, Exception("User not logged in"))
            return null
        }

        return usersCollection.document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    callback(null, error)
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    callback(null, null)
                    return@addSnapshotListener
                }

                val profile = UserProfile(
                    uid = user.uid,
                    name = snapshot.getString("nickname") ?: "",
                    status = runCatching {
                        UserStatus.valueOf(snapshot.getString("status") ?: "")
                    }.getOrDefault(UserStatus.FREE),
                    avatarIndex = snapshot.getLong("avatarIndex")?.toInt() ?: 0,
                    tag = snapshot.getLong("tag")?.toInt() ?: 0
                    // Add other fields as necessary
                )
                callback(profile, null)
            }
    }
}
