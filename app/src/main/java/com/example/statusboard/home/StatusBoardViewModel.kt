package com.example.statusboard.home

import androidx.lifecycle.ViewModel
import com.example.statusboard.data.usersCollection
import com.example.statusboard.domain.model.UserProfile
import com.example.statusboard.domain.model.UserStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StatusBoardViewModel : ViewModel() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _me = MutableStateFlow<UserProfile?>(null)
    val me: StateFlow<UserProfile?> = _me.asStateFlow()

    private val _friends = MutableStateFlow<List<UserProfile>>(emptyList())
    val friends: StateFlow<List<UserProfile>> = _friends.asStateFlow()

    private var meListener: ListenerRegistration? = null
    private var friendsListener: ListenerRegistration? = null

    init {
        subscribeToCurrentUser()
        subscribeToFriends()
    }

    private fun subscribeToCurrentUser() {
        val user = auth.currentUser ?: return
        meListener?.remove()

        meListener = usersCollection
            .document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    _me.value = null
                    return@addSnapshotListener
                }

                val nickname = snapshot.getString("nickname") ?: ""
                val statusString = snapshot.getString("status") ?: UserStatus.FREE.name
                val status = runCatching { UserStatus.valueOf(statusString) }
                    .getOrElse { UserStatus.FREE }

                val avatarIndex = snapshot.getLong("avatarIndex")?.toInt() ?: 0
                val tag = snapshot.getLong("tag")?.toInt() ?: 0

                _me.value = UserProfile(
                    uid = user.uid,
                    name = nickname,
                    status = status,
                    avatarIndex = avatarIndex,
                    tag = tag
                )
            }
    }

    private fun subscribeToFriends() {
        val user = auth.currentUser ?: return
        friendsListener?.remove()

        val friendsCollection = usersCollection
            .document(user.uid)
            .collection("friends")

        friendsListener = friendsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    _friends.value = emptyList()
                    return@addSnapshotListener
                }

                val list = snapshot.documents.map { friendDoc ->
                    val friendUid = friendDoc.getString("uid") ?: friendDoc.id
                    friendUid
                }

                if (list.isEmpty()) {
                    _friends.value = emptyList()
                    return@addSnapshotListener
                }

                // Fetch user docs for each friend
                firestore.runBatch { batch -> }
                usersCollection
                    .whereIn("uid", list)
                    .get()
                    .addOnSuccessListener { qs ->
                        val friendsProfiles = qs.documents.mapNotNull { doc ->
                            val uid = doc.getString("uid") ?: doc.id
                            val nickname = doc.getString("nickname") ?: ""
                            val statusString =
                                doc.getString("status") ?: UserStatus.FREE.name
                            val status = runCatching { UserStatus.valueOf(statusString) }
                                .getOrElse { UserStatus.FREE }

                            val avatarIndex = doc.getLong("avatarIndex")?.toInt() ?: 0
                            val tag = doc.getLong("tag")?.toInt() ?: 0

                            UserProfile(
                                uid = uid,
                                name = nickname,
                                status = status,
                                avatarIndex = avatarIndex,
                                tag = tag
                            )
                        }

                        _friends.value = friendsProfiles
                    }
                    .addOnFailureListener {
                        _friends.value = emptyList()
                    }
            }
    }

    override fun onCleared() {
        super.onCleared()
        meListener?.remove()
        friendsListener?.remove()
    }
}