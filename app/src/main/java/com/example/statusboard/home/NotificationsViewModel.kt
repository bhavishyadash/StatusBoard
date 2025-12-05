package com.example.statusboard.home

import androidx.lifecycle.ViewModel
import com.example.statusboard.domain.model.FriendRequest
import com.example.statusboard.domain.model.FriendRequestStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Firestore collection: "friend_requests"
    private val requestsCollection = firestore.collection("friendRequests")

    private val _incoming = MutableStateFlow<List<FriendRequest>>(emptyList())
    val incoming: StateFlow<List<FriendRequest>> = _incoming

    private var listener: ListenerRegistration? = null

    init {
        subscribeToIncoming()
    }

    private fun subscribeToIncoming() {
        listener?.remove()

        val currentUser = auth.currentUser ?: return

        listener = requestsCollection
            .whereEqualTo("toUid", currentUser.uid)      // EXACTLY as in the badge code
            .whereEqualTo("status", "PENDING")           // EXACTLY as in the badge code
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.map { doc ->
                    FriendRequest(
                        id = doc.id,
                        fromUid = doc.getString("fromUid") ?: "",
                        fromNickname = doc.getString("fromNickname") ?: "",
                        status = FriendRequestStatus.PENDING,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }

                _incoming.value = list.sortedByDescending { it.createdAt }
            }
    }

    fun accept(request: FriendRequest) {
        requestsCollection
            .document(request.id)
            .update("status", FriendRequestStatus.ACCEPTED.name)
    }

    fun reject(request: FriendRequest) {
        requestsCollection
            .document(request.id)
            .update("status", FriendRequestStatus.REJECTED.name)
    }

    override fun onCleared() {
        listener?.remove()
        super.onCleared()
    }
}