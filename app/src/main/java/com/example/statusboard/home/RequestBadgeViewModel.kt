package com.example.statusboard.home

import androidx.lifecycle.ViewModel
import com.example.statusboard.domain.model.FriendRequestStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RequestsBadgeViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val requestsCollection = Firebase.firestore.collection("friendRequests")

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount

    private var listener: ListenerRegistration? = null

    init {
        subscribeToPendingRequests()
    }

    private fun subscribeToPendingRequests() {
        val user = auth.currentUser ?: return

        listener?.remove()

        listener = requestsCollection
            .whereEqualTo("toUid", user.uid)
            .whereEqualTo("status", FriendRequestStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _pendingCount.value = snapshot.size()
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}