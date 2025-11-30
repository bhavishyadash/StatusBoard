package com.example.statusboard.friends

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.statusboard.domain.model.UserProfile
import com.example.statusboard.domain.model.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private enum class ExistingRelation {
    NONE,
    REQUEST_PENDING,
    ALREADY_FRIEND
}

@Composable
fun AddFriendScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { Firebase.firestore }

    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<UserProfile?>(null) }
    var isSending by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var relation by remember { mutableStateOf(ExistingRelation.NONE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Add Friends",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Search by email or nickname.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Email or nickname") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorText != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorText!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }

            Button(
                onClick = {
                    if (query.isBlank()) {
                        errorText = "Enter an email or nickname."
                        return@Button
                    }

                    errorText = null
                    isSearching = true
                    searchResult = null
                    relation = ExistingRelation.NONE

                    val trimmed = query.trim()

                    val usersCol = db.collection("users")

                    // 1) Try email
                    usersCol
                        .whereEqualTo("email", trimmed)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { snap ->
                            if (!snap.isEmpty) {
                                val doc = snap.documents.first()
                                val profile = doc.toUserProfile()
                                searchResult = profile
                                isSearching = false
                                checkExistingRelation(auth, db, profile.uid) {
                                    relation = it
                                }
                            } else {
                                // 2) Try nickname
                                usersCol
                                    .whereEqualTo("nickname", trimmed)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener { snap2 ->
                                        if (!snap2.isEmpty) {
                                            val doc = snap2.documents.first()
                                            val profile = doc.toUserProfile()
                                            searchResult = profile
                                            checkExistingRelation(auth, db, profile.uid) {
                                                relation = it
                                            }
                                        } else {
                                            errorText =
                                                "No user found with that email or nickname."
                                        }
                                        isSearching = false
                                    }
                                    .addOnFailureListener { e ->
                                        errorText = e.localizedMessage ?: "Search failed."
                                        isSearching = false
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            errorText = e.localizedMessage ?: "Search failed."
                            isSearching = false
                        }
                },
                enabled = !isSearching,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isSearching) "Searching..." else "Search")
            }
        }

        Spacer(Modifier.height(24.dp))

        val currentUser = auth.currentUser

        searchResult?.let { user ->
            if (currentUser != null && user.uid == currentUser.uid) {
                Text(
                    text = "That's you 🙂 You can't send a request to yourself.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                FriendSearchResultCard(
                    user = user,
                    existingRelation = relation,
                    isSending = isSending,
                    onSendRequest = {
                        val me = auth.currentUser
                            ?: run {
                                Toast.makeText(
                                    context,
                                    "You must be logged in.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@FriendSearchResultCard
                            }

                        isSending = true

                        // Get my nickname to store in request
                        db.collection("users").document(me.uid)
                            .get()
                            .addOnSuccessListener { meDoc ->
                                val myNickname =
                                    meDoc.getString("nickname") ?: (me.email ?: "")

                                val requestId =
                                    db.collection("friendRequests").document().id

                                val requestData = mapOf(
                                    "fromUid" to me.uid,
                                    "fromNickname" to myNickname,
                                    "toUid" to user.uid,
                                    "toNickname" to user.name,
                                    "status" to "PENDING",
                                    "createdAt" to System.currentTimeMillis()
                                )

                                db.collection("friendRequests")
                                    .document(requestId)
                                    .set(requestData)
                                    .addOnSuccessListener {
                                        isSending = false
                                        relation = ExistingRelation.REQUEST_PENDING
                                        Toast.makeText(
                                            context,
                                            "Friend request sent!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        isSending = false
                                        Toast.makeText(
                                            context,
                                            e.localizedMessage
                                                ?: "Failed to send request.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                isSending = false
                                Toast.makeText(
                                    context,
                                    e.localizedMessage ?: "Failed to send request.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                )
            }
        }
    }
}

// ───────── Helpers ─────────

private fun com.google.firebase.firestore.DocumentSnapshot.toUserProfile(): UserProfile {
    val uid = id
    val nickname = getString("nickname") ?: ""
    val statusString = getString("status") ?: UserStatus.FREE.name
    val status = runCatching { UserStatus.valueOf(statusString) }
        .getOrDefault(UserStatus.FREE)

    return UserProfile(
        uid = uid,
        name = nickname,
        status = status
    )
}

/**
 * Checks whether the found user is:
 *  - already a friend
 *  - has a pending request from current user
 *  - or none of the above
 */
private fun checkExistingRelation(
    auth: FirebaseAuth,
    db: com.google.firebase.firestore.FirebaseFirestore,
    otherUid: String,
    onResult: (ExistingRelation) -> Unit
) {
    val me = auth.currentUser ?: return onResult(ExistingRelation.NONE)
    val usersCol = db.collection("users")

    // 1) Check if already friends
    usersCol.document(me.uid)
        .collection("friends")
        .document(otherUid)
        .get()
        .addOnSuccessListener { friendDoc ->
            if (friendDoc.exists()) {
                onResult(ExistingRelation.ALREADY_FRIEND)
            } else {
                // 2) Check pending outgoing request
                db.collection("friendRequests")
                    .whereEqualTo("fromUid", me.uid)
                    .whereEqualTo("toUid", otherUid)
                    .whereEqualTo("status", "PENDING")
                    .limit(1)
                    .get()
                    .addOnSuccessListener { reqSnap ->
                        if (!reqSnap.isEmpty) {
                            onResult(ExistingRelation.REQUEST_PENDING)
                        } else {
                            onResult(ExistingRelation.NONE)
                        }
                    }
                    .addOnFailureListener {
                        onResult(ExistingRelation.NONE)
                    }
            }
        }
        .addOnFailureListener {
            onResult(ExistingRelation.NONE)
        }
}

@Composable
private fun FriendSearchResultCard(
    user: UserProfile,
    existingRelation: ExistingRelation,
    isSending: Boolean,
    onSendRequest: () -> Unit
) {
    val buttonEnabled =
        existingRelation == ExistingRelation.NONE && !isSending

    val buttonText = when {
        existingRelation == ExistingRelation.ALREADY_FRIEND -> "Already friends"
        existingRelation == ExistingRelation.REQUEST_PENDING -> "Request pending"
        isSending -> "Sending..."
        else -> "Send Friend Request"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = user.name.ifBlank { "(no nickname)" },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Status: ${user.status.emoji} ${user.status.label}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onSendRequest,
                enabled = buttonEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }

            if (existingRelation == ExistingRelation.REQUEST_PENDING) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "They haven't responded yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}