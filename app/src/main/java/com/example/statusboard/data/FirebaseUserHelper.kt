package com.example.statusboard.data

import com.example.statusboard.domain.model.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.random.Random

private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

val usersCollection: CollectionReference
    get() = firestore.collection("users")

/**
 * Ensure a Firestore user doc exists for the current user.
 */
fun createFirestoreUserIfNeeded(onComplete: (Boolean) -> Unit = {}) {
    val user = auth.currentUser ?: run {
        onComplete(false)
        return
    }

    val docRef = usersCollection.document(user.uid)

    docRef.get()
        .addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                onComplete(true)
            } else {
                val tag = Random.nextInt(1000, 9999)
                val data = hashMapOf(
                    "uid" to user.uid,
                    "email" to (user.email ?: ""),
                    "nickname" to "",
                    "status" to UserStatus.FREE.name,
                    "avatarIndex" to 0,
                    "tag" to tag
                )
                docRef.set(data)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
        }
        .addOnFailureListener { onComplete(false) }
}

/**
 * Save / update nickname and ensure the user has a stable tag.
 */
fun saveNickname(
    nickname: String,
    onComplete: (Boolean) -> Unit
) {
    val user = auth.currentUser ?: run {
        onComplete(false)
        return
    }

    val docRef = usersCollection.document(user.uid)

    firestore.runTransaction { tx ->
        val snapshot = tx.get(docRef)

        val existingTag = snapshot.getLong("tag")?.toInt()
        val tag = existingTag ?: Random.nextInt(1000, 9999)

        val currentStatus = snapshot.getString("status") ?: UserStatus.FREE.name
        val currentAvatarIndex = snapshot.getLong("avatarIndex") ?: 0L

        val data = hashMapOf(
            "uid" to user.uid,
            "email" to (user.email ?: ""),
            "nickname" to nickname,
            "status" to currentStatus,
            "avatarIndex" to currentAvatarIndex,
            "tag" to tag
        )

        tx.set(docRef, data, SetOptions.merge())
    }.addOnSuccessListener {
        onComplete(true)
    }.addOnFailureListener {
        onComplete(false)
    }
}

/**
 * Update the current user's status (Free / DND / Away / Sleeping).
 */
fun updateUserStatus(
    status: UserStatus,
    onComplete: (Boolean) -> Unit = {}
) {
    val user = auth.currentUser ?: run {
        onComplete(false)
        return
    }

    usersCollection.document(user.uid)
        .update("status", status.name)
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
}

/**
 * Update avatar index if you support avatar selection.
 */
fun updateAvatarIndex(
    avatarIndex: Int,
    onComplete: (Boolean) -> Unit = {}
) {
    val user = auth.currentUser ?: run {
        onComplete(false)
        return
    }

    usersCollection.document(user.uid)
        .update("avatarIndex", avatarIndex)
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
}