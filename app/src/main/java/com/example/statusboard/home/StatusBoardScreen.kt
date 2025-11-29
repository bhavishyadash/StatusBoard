package com.example.statusboard.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.statusboard.domain.model.UserProfile
import com.example.statusboard.domain.model.UserStatus

@Composable
fun StatusBoardScreen(
    viewModel: StatusBoardViewModel = viewModel()
) {
    val meState by viewModel.me.collectAsState()
    val friendsState by viewModel.friends.collectAsState()

    val me = meState ?: UserProfile(
        uid = "",
        name = "Loading...",
        status = UserStatus.FREE
    )
    val friends = friendsState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F6))
            .padding(16.dp)
    ) {
        Column {

            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status Board",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { /* TODO: navigate to settings */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    IconButton(onClick = { /* TODO: navigate to Add Friend */ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add friend"
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // YOU CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111318)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "You",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFCCCCCC)
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4C7DFF))
                        )

                        Spacer(Modifier.width(14.dp))

                        Column {
                            Text(
                                text = me.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Status: ${me.status.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB0B0B0)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row {
                        // Use values() instead of entries for compatibility
                        UserStatus.values().forEach { status ->
                            StatusChip(
                                status = status,
                                selected = status == me.status,
                                onClick = { viewModel.changeStatus(status) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Friends",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(Modifier.height(8.dp))

            if (friends.isEmpty()) {
                Text(
                    text = "No friends yet. Tap + to add someone!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(friends) { friend ->
                        FriendCard(friend = friend)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendCard(friend: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E7FF))
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = friend.name.ifBlank { "(no nickname)" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = friend.status.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: UserStatus,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) Color(0xFF4C7DFF) else Color(0xFF1C1F24),
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = status.label,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) Color.White else Color(0xFFCCCCCC)
            )
        }
    }
}