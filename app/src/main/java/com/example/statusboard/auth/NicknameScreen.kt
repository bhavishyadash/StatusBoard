package com.example.statusboard.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.statusboard.data.saveNickname
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

@Composable
fun NicknameScreen(
    onDone: () -> Unit
) {
    val context = LocalContext.current

    var nickname by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Prefill nickname if it already exists in Firestore
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: return@LaunchedEffect
        Firebase.firestore.collection("users")
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
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
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

                        isSaving = true
                        saveNickname(nickname.trim()) { success ->
                            // This callback ALWAYS runs because we handle both
                            isSaving = false
                            if (success) {
                                onDone()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Couldn't save nickname. Check your connection.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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