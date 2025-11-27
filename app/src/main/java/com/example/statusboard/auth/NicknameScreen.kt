package com.example.statusboard.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.statusboard.data.saveNickname

@Composable
fun NicknameScreen(
    onDone: () -> Unit
) {
    val context = LocalContext.current
    var nickname by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "What should people call you?",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Nickname") }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (nickname.isBlank()) {
                    Toast.makeText(context, "Please enter a nickname", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isSaving = true
                saveNickname(nickname.trim()) { success ->
                    isSaving = false
                    if (success) {
                        onDone()
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to save nickname",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(text = if (isSaving) "Saving..." else "Yeah, that Looks Good!")
        }
    }
}
