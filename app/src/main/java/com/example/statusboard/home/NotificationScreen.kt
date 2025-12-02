package com.example.statusboard.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.statusboard.data.UserRepository
import com.example.statusboard.domain.model.NotificationItem
import com.example.statusboard.domain.model.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No notifications yet",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    NotificationItemCard(
                        item = notif,
                        onAccept = { requestId ->
                            if (requestId != null) {
                                UserRepository.respondToFriendRequest(
                                    requestId = requestId,
                                    accept = true
                                ) { _, _ -> }
                            }
                            viewModel.markAsRead(notif.id)
                        },
                        onReject = { requestId ->
                            if (requestId != null) {
                                UserRepository.respondToFriendRequest(
                                    requestId = requestId,
                                    accept = false
                                ) { _, _ -> }
                            }
                            viewModel.markAsRead(notif.id)
                        },
                        onTap = {
                            viewModel.markAsRead(notif.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    item: NotificationItem,
    onAccept: (String?) -> Unit,
    onReject: (String?) -> Unit,
    onTap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (!item.isRead)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onTap
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (item.type) {
                        NotificationType.FRIEND_REQUEST -> Icons.Default.PersonAdd
                        NotificationType.FRIEND_ACCEPTED -> Icons.Default.CheckCircle
                        NotificationType.FRIEND_REJECTED -> Icons.Default.Close
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when (item.type) {
                            NotificationType.FRIEND_REQUEST -> "Friend request"
                            NotificationType.FRIEND_ACCEPTED -> "Friend request accepted"
                            NotificationType.FRIEND_REJECTED -> "Friend request rejected"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    val fromPart =
                        if (item.fromName.isNotBlank()) item.fromName else "Someone"

                    Text(
                        text = "$fromPart ${item.message}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (!item.isRead) {
                    Spacer(Modifier.width(8.dp))
                    Badge()
                }
            }

            // Actions for friend requests
            if (item.type == NotificationType.FRIEND_REQUEST && item.requestId != null) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { onAccept(item.requestId) }
                    ) {
                        Text("Accept")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { onReject(item.requestId) }
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}