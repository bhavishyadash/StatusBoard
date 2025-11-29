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

@Composable
fun NicknameScreen(
    onDone: () -> Unit
) {
    val context = LocalContext.current

    var nickname by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

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

                        // 🔥 NO FIREBASE. JUST GO AHEAD.
                        isSaving = true
                        Toast.makeText(
                            context,
                            "Nickname set to \"$nickname\"",
                            Toast.LENGTH_SHORT
                        ).show()

                        // immediately navigate forward
                        onDone()
                        isSaving = false
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