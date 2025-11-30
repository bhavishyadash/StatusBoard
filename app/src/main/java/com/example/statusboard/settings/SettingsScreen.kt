package com.example.statusboard.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.statusboard.ui.theme.ThemeMode
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.RadioButton

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onEditNickname: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            // ───────── TOP BAR ─────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(8.dp))

            // ───────── ACCOUNT CARD ─────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    // Email block – clickable (change email coming later)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast
                                    .makeText(
                                        context,
                                        "Change email coming soon 👀",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                    ) {
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = currentUser?.email ?: "Not signed in",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Divider()

                    Spacer(Modifier.height(12.dp))

                    // Change nickname row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditNickname() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Change nickname",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Update the name your friends see.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ───────── APPEARANCE CARD ─────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Choose how StatusBoard follows light / dark.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(Modifier.height(12.dp))

                    ThemeRadioRow(
                        label = "Use system setting",
                        description = "Match your device’s theme.",
                        selected = currentTheme == ThemeMode.SYSTEM,
                        onSelect = { onThemeChange(ThemeMode.SYSTEM) }
                    )

                    ThemeRadioRow(
                        label = "Light",
                        description = "Always use light mode.",
                        selected = currentTheme == ThemeMode.LIGHT,
                        onSelect = { onThemeChange(ThemeMode.LIGHT) }
                    )

                    ThemeRadioRow(
                        label = "Dark",
                        description = "Always use dark mode.",
                        selected = currentTheme == ThemeMode.DARK,
                        onSelect = { onThemeChange(ThemeMode.DARK) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ───────── ABOUT CARD ─────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "StatusBoard",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "A presence board for you and your friends.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "v0.1 (dev build)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ───────── LOG OUT BUTTON ─────────
            OutlinedButton(
                onClick = {
                    auth.signOut()
                    Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                    onLogoutSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Log out")
            }
        }
    }
}

@Composable
private fun ThemeRadioRow(
    label: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}