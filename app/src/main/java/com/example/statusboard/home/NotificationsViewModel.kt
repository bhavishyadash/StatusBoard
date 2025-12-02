package com.example.statusboard.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.statusboard.domain.model.NotificationItem
import com.example.statusboard.domain.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var listener: ListenerRegistration? = null

    init {
        subscribe()
    }

    private fun subscribe() {
        val user = auth.currentUser ?: return
        listener?.remove()

        listener = firestore.collection("users")
            .document(user.uid)
            .collection("notifications")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.mapNotNull { doc ->
                    val typeString = doc.getString("type") ?: return@mapNotNull null
                    val type = runCatching { NotificationType.valueOf(typeString) }
                        .getOrElse { NotificationType.FRIEND_REQUEST }

                    NotificationItem(
                        id = doc.id,
                        type = type,
                        fromUserId = doc.getString("fromUserId") ?: "",
                        fromName = doc.getString("fromName") ?: "",
                        message = doc.getString("message") ?: "",
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAt = doc.getTimestamp("createdAt")
                            ?: com.google.firebase.Timestamp.now(),
                        requestId = doc.getString("requestId")
                    )
                }

                _notifications.value = list
                _unreadCount.value = list.count { !it.isRead }
            }
    }

    fun markAsRead(notificationId: String) {
        val user = auth.currentUser ?: return

        viewModelScope.launch {
            firestore.collection("users")
                .document(user.uid)
                .collection("notifications")
                .document(notificationId)
                .update("isRead", true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}