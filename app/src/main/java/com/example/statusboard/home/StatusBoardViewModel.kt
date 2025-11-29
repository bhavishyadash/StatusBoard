package com.example.statusboard.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.statusboard.domain.model.UserProfile
import com.example.statusboard.domain.model.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatusBoardViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val usersCollection = Firebase.firestore.collection("users")

    private val _me = MutableStateFlow<UserProfile?>(null)
    val me: StateFlow<UserProfile?> = _me

    private val _friends = MutableStateFlow<List<UserProfile>>(emptyList())
    val friends: StateFlow<List<UserProfile>> = _friends

    private var meListener: ListenerRegistration? = null
    private var friendsListener: ListenerRegistration? = null


    init {
        subscribeToCurrentUser()
        // TODO later: loadFriends()
    }

    private fun subscribeToCurrentUser() {
        val user = auth.currentUser ?: return

        // Clean up any previous listener
        meListener?.remove()

        meListener = usersCollection.document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    return@addSnapshotListener
                }

                val nickname = snapshot.getString("nickname") ?: ""
                val statusString = snapshot.getString("status") ?: UserStatus.FREE.name

                val status = runCatching {
                    UserStatus.valueOf(statusString)
                }.getOrDefault(UserStatus.FREE)

                _me.value = UserProfile(
                    uid = user.uid,
                    name = nickname,
                    status = status
                )
            }
    }
    private fun subscribeToFriends() {
        val user = auth.currentUser ?: return
        friendsListener?.remove()

        friendsListener = usersCollection
            .document(user.uid)
            .collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString("uid") ?: doc.id
                    val nickname = doc.getString("nickname") ?: ""
                    val statusString = doc.getString("status") ?: UserStatus.FREE.name
                    val status = runCatching { UserStatus.valueOf(statusString) }
                        .getOrDefault(UserStatus.FREE)

                    UserProfile(
                        uid = uid,
                        name = nickname,
                        status = status
                    )
                }

                _friends.value = list
            }
    }

    fun changeStatus(newStatus: UserStatus) {
        val user = auth.currentUser ?: return

        // Update local state immediately so UI feels snappy
        _me.value = _me.value?.copy(status = newStatus)

        // Push to Firestore
        viewModelScope.launch {
            usersCollection.document(user.uid)
                .set(
                    mapOf(
                        "status" to newStatus.name,
                        "lastUpdated" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
        }
    }

    override fun onCleared() {
        super.onCleared()
        meListener?.remove()
    }
}