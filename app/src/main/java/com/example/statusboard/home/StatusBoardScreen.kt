package com.example.statusboard.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.statusboard.domain.model.UserProfile
import com.example.statusboard.domain.model.UserStatus

@Composable
fun StatusBoardScreen() {

    // TEMP dummy user until Firestore is wired
    val currentUser = UserProfile(
        name = "Bhavishya",
        status = UserStatus.FREE
    )

    val friends = listOf(
        UserProfile(name = "Alice", status = UserStatus.DND),
        UserProfile(name = "Bob", status = UserStatus.FREE),
        UserProfile(name = "Charlie", status = UserStatus.SLEEPING)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4))
            .padding(16.dp)
    ) {
        Column {

            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Status Board",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }

            Spacer(Modifier.height(16.dp))

            // YOU CARD
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("You", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Round avatar placeholder
                        Box(
                            modifier = Modifier
                                .size(55.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD0D0FF))
                        )
                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                currentUser.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                "Status: ${currentUser.status.label}",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row {
                        UserStatus.entries.forEach { status ->
                            StatusChip(
                                status = status,
                                selected = currentUser.status == status,
                                onClick = { /* TODO: update Firestore */ }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Friends",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(friends) { friend ->
                    FriendCard(friend)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun FriendCard(friend: UserProfile) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCCE2FF))
            )

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    friend.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    friend.status.label,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    status: UserStatus,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFF4660F2) else Color(0xFFE8E8E8),
        modifier = Modifier
            .padding(end = 6.dp)
            .height(35.dp)
            .clip(RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = status.label,
                color = if (selected) Color.White else Color.Black
            )
        }
    }
}
