package com.example.statusboard.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Shared auth + collection helpers
private val auth: FirebaseAuth
    get() = FirebaseAuth.getInstance()

private val usersCollection
    get() = Firebase.firestore.collection("users")

fun createFirestoreUserIfNeeded() {
    val user = auth.currentUser ?: return
    val docRef = usersCollection.document(user.uid)

    docRef.get().addOnSuccessListener { snapshot ->
        if (!snapshot.exists()) {
            val userData = mapOf(
                "uid" to user.uid,
                "email" to user.email,
                "photoUrl" to user.photoUrl?.toString(),
                "nickname" to (user.displayName ?: ""),
                "status" to "FREE",
                "lastUpdated" to System.currentTimeMillis()
            )
            docRef.set(userData)
        }
    }
}

fun saveNickname(nickname: String, onComplete: (Boolean) -> Unit) {
    val user = auth.currentUser ?: run {
        onComplete(false)
        return
    }

    val docRef = usersCollection.document(user.uid)

    docRef.update(
        mapOf(
            "nickname" to nickname,
            "lastUpdated" to System.currentTimeMillis()
        )
    ).addOnSuccessListener {
        onComplete(true)
    }.addOnFailureListener {
        onComplete(false)
    }
}
