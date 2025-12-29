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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.statusboard.domain.model.UserProfile
import com.example.statusboard.domain.model.UserStatus

@Composable
fun StatusBoardScreen(
    onOpenSettings: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onAddFriend: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    viewModel: StatusBoardViewModel = viewModel()
) {
    val me by viewModel.userProfile.collectAsStateWithLifecycle()
    val friends by viewModel.friends.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopBar(
                onOpenSettings = onOpenSettings,
                onOpenNotifications = onOpenNotifications,
                onAddFriend = onAddFriend
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CurrentUserCard(
                me = me,
                onOpenProfile = onOpenProfile,
                onStatusSelected = { viewModel.changeStatus(it) }
            )

            FriendsHeader(onAddFriend = onAddFriend)

            FriendsList(friends = friends)
        }
    }
}

@Composable
private fun TopBar(
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    onAddFriend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Status Board",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onAddFriend) {
            Icon(Icons.Default.Add, contentDescription = "Add Friend")
        }

        IconButton(onClick = onOpenNotifications) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
        }

        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}

@Composable
private fun CurrentUserCard(
    me: UserProfile?,
    onOpenProfile: () -> Unit,
    onStatusSelected: (UserStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = me?.nickname?.firstOrNull()?.uppercase() ?: "Y",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = me?.nickname ?: "You", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Status: ${me?.status?.name ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                TextButton(onClick = onOpenProfile) {
                    Text("Profile")
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UserStatus.entries.forEach { status ->
                    StatusChip(
                        label = status.name,
                        selected = me?.status == status,
                        onClick = { onStatusSelected(status) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FriendsHeader(onAddFriend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Friends", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        IconButton(onClick = onAddFriend) {
            Icon(Icons.Default.Add, contentDescription = "Add Friend")
        }
    }
}

@Composable
private fun FriendsList(friends: List<UserProfile>) {
    if (friends.isEmpty()) {
        Text(
            text = "No friends yet. Tap + to add someone!",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    LazyColumn {
        items(friends, key = { it.uid }) { friend ->
            FriendRow(friend)
        }
    }
}

@Composable
private fun FriendRow(friend: UserProfile) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = friend.nickname.firstOrNull()?.uppercase() ?: "?",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(friend.nickname, fontWeight = FontWeight.SemiBold)
            Text(
                text = friend.status.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}