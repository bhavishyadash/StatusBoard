package com.example.statusboard.auth

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun NicknameScreen(
    onDone: () -> Unit
) {
    val context = LocalContext.current

    var nickname by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Optional: prefill nickname if it already exists
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: return@LaunchedEffect
        Firebase.firestore
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { snap ->
                val existing = snap.getString("nickname")
                if (!existing.isNullOrBlank()) {
                    nickname = existing
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "What should people call you?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (nickname.isBlank()) {
                            Toast.makeText(
                                context,
                                "Nickname can't be empty",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        val auth = FirebaseAuth.getInstance()
                        val user = auth.currentUser
                        if (user == null) {
                            Toast.makeText(
                                context,
                                "You are not logged in",
                                Toast.LENGTH_SHORT
                            ).show()
                            // still let them through so app isn't blocked
                            onDone()
                            return@Button
                        }

                        isSaving = true

                        val data = mapOf(
                            "uid" to user.uid,
                            "email" to user.email,
                            "nickname" to nickname.trim(),
                            "status" to "FREE",
                            "lastUpdated" to System.currentTimeMillis()
                        )

                        Firebase.firestore
                            .collection("users")
                            .document(user.uid)
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener {
                                isSaving = false
                                Toast.makeText(
                                    context,
                                    "Nickname saved!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onDone()  // ✅ go to home
                            }
                            .addOnFailureListener { e ->
                                isSaving = false
                                Toast.makeText(
                                    context,
                                    "Couldn't save nickname: ${e.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // ❗ still navigate so you NEVER get stuck
                                onDone()
                            }
                    },
                    enabled = !isSaving && nickname.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(if (isSaving) "Saving..." else "Continue")
                }
            }
        }
    }
}