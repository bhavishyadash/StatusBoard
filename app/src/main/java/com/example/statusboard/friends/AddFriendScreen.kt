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
import androidx.compose.ui.unit.dp
import com.example.statusboard.domain.model.UserProfile
import com.example.statusboard.domain.model.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
    var isAdding by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Add Friends",
                style = MaterialTheme.typography.titleLarge.copy(
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

                        // First try by email
                        db.collection("users")
                            .whereEqualTo("email", query.trim())
                            .limit(1)
                            .get()
                            .addOnSuccessListener { snap ->
                                if (!snap.isEmpty) {
                                    val doc = snap.documents.first()
                                    val statusString = doc.getString("status") ?: UserStatus.FREE.name
                                    val status = runCatching {
                                        UserStatus.valueOf(statusString)
                                    }.getOrDefault(UserStatus.FREE)

                                    searchResult = UserProfile(
                                        uid = doc.id,
                                        name = doc.getString("nickname") ?: "",
                                        status = status
                                    )
                                    isSearching = false
                                } else {
                                    // Then try by nickname
                                    db.collection("users")
                                        .whereEqualTo("nickname", query.trim())
                                        .limit(1)
                                        .get()
                                        .addOnSuccessListener { snap2 ->
                                            if (!snap2.isEmpty) {
                                                val doc = snap2.documents.first()
                                                val statusString = doc.getString("status") ?: UserStatus.FREE.name
                                                val status = runCatching {
                                                    UserStatus.valueOf(statusString)
                                                }.getOrDefault(UserStatus.FREE)

                                                searchResult = UserProfile(
                                                    uid = doc.id,
                                                    name = doc.getString("nickname") ?: "",
                                                    status = status
                                                )
                                            } else {
                                                errorText = "No user found with that email or nickname."
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

            searchResult?.let { user ->
                FriendSearchResultCard(
                    user = user,
                    isAdding = isAdding,
                    onAddClick = {
                        val currentUser = auth.currentUser
                        if (currentUser == null) {
                            Toast.makeText(
                                context,
                                "You must be logged in.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@FriendSearchResultCard
                        }

                        if (user.uid == currentUser.uid) {
                            Toast.makeText(
                                context,
                                "You can't add yourself.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@FriendSearchResultCard
                        }

                        isAdding = true

                        val friendDoc = db.collection("users")
                            .document(currentUser.uid)
                            .collection("friends")
                            .document(user.uid)

                        val friendData = mapOf(
                            "uid" to user.uid,
                            "nickname" to user.name,
                            "status" to user.status.name
                        )

                        friendDoc.set(friendData)
                            .addOnSuccessListener {
                                isAdding = false
                                Toast.makeText(
                                    context,
                                    "Friend added!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Optionally go back
                                // onBack()
                            }
                            .addOnFailureListener { e ->
                                isAdding = false
                                Toast.makeText(
                                    context,
                                    e.localizedMessage ?: "Failed to add friend.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                )
            }
        }
    }
}

@Composable
private fun FriendSearchResultCard(
    user: UserProfile,
    isAdding: Boolean,
    onAddClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = user.name.ifBlank { "(no nickname)" },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Status: ${user.status.label}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAddClick,
                enabled = !isAdding,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isAdding) "Adding..." else "Add Friend")
            }
        }
    }
}