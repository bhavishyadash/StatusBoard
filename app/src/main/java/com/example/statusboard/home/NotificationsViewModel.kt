package com.example.statusboard.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.statusboard.domain.model.FriendRequest
import com.example.statusboard.domain.model.FriendRequestStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.statusboard.domain.model.UserStatus


class NotificationsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val requestsCollection = Firebase.firestore.collection("friendRequests")
    private val usersCollection = Firebase.firestore.collection("users")

    private val _incoming = MutableStateFlow<List<FriendRequest>>(emptyList())
    val incoming: StateFlow<List<FriendRequest>> = _incoming

    private var listener: ListenerRegistration? = null

    init {
        subscribeToIncomingRequests()
    }

    private fun subscribeToIncomingRequests() {
        val user = auth.currentUser ?: return
        listener?.remove()

        listener = requestsCollection
            .whereEqualTo("toUid", user.uid)
            .whereEqualTo("status", FriendRequestStatus.PENDING.name)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener

                val list = snap.documents.map { doc ->
                    FriendRequest(
                        id = doc.id,
                        fromUid = doc.getString("fromUid") ?: "",
                        fromNickname = doc.getString("fromNickname") ?: "",
                        toUid = doc.getString("toUid") ?: "",
                        toNickname = doc.getString("toNickname") ?: "",
                        status = FriendRequestStatus.PENDING,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }
                _incoming.value = list
            }
    }

    fun accept(request: FriendRequest) {
        val me = auth.currentUser ?: return
        val db = Firebase.firestore

        viewModelScope.launch {
            val batch = db.batch()

            val reqRef = requestsCollection.document(request.id)
            batch.update(reqRef, "status", FriendRequestStatus.ACCEPTED.name)

            val meRef = usersCollection.document(me.uid)
            val themRef = usersCollection.document(request.fromUid)

            batch.set(
                meRef.collection("friends").document(request.fromUid),
                mapOf(
                    "uid" to request.fromUid,
                    "nickname" to request.fromNickname,
                    "status" to UserStatus.FREE.name // initial, will change as they update
                )
            )

            batch.set(
                themRef.collection("friends").document(me.uid),
                mapOf(
                    "uid" to me.uid,
                    "nickname" to request.toNickname,
                    "status" to UserStatus.FREE.name
                )
            )

            batch.commit()
        }
    }

    fun reject(request: FriendRequest) {
        viewModelScope.launch {
            requestsCollection
                .document(request.id)
                .update("status", FriendRequestStatus.REJECTED.name)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}