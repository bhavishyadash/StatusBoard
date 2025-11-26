package com.example.statusboard.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

fun createFirestoreUserIfNeeded() {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser ?: return
    val db = Firebase.firestore

    val docRef = db.collection("users").document(user.uid)

    docRef.get().addOnSuccessListener { snapshot ->
        if (!snapshot.exists()) {
            val userData = mapOf(
                "uid" to user.uid,
                "name" to (user.displayName ?: ""),
                "email" to user.email,
                "photoUrl" to user.photoUrl?.toString(),
                "status" to "FREE",
                "lastUpdated" to System.currentTimeMillis()
            )
            docRef.set(userData)
        }
    }
}
