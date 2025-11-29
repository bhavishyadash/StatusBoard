package com.example.statusboard.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private val auth: FirebaseAuth
    get() = FirebaseAuth.getInstance()

private val usersCollection
    get() = Firebase.firestore.collection("users")

// Make sure there's a user doc in Firestore
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

// Save nickname (creates doc if it doesn't exist)
fun saveNickname(nickname: String, onComplete: (Boolean) -> Unit) {
    val user = auth.currentUser ?: run {
        onComplete(false)
        return
    }

    val docRef = usersCollection.document(user.uid)

    val data = mapOf(
        "nickname" to nickname,
        "lastUpdated" to System.currentTimeMillis()
    )

    // merge = create or update; this avoids "no document" failures
    docRef.set(data, SetOptions.merge())
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
}