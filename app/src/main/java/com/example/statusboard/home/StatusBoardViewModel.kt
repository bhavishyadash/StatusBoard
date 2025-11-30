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

    // Realtime listeners
    private var meListener: ListenerRegistration? = null
    private var friendsListener: ListenerRegistration? = null

    init {
        subscribeToCurrentUser()
        subscribeToFriends()
    }

    /** ─────────────────────────────────────────────────────────────
     *  LISTEN TO CURRENT USER DOCUMENT
     *  ───────────────────────────────────────────────────────────── */
    private fun subscribeToCurrentUser() {
        val user = auth.currentUser ?: return

        // remove old listener if exists
        meListener?.remove()

        meListener = usersCollection
            .document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val nickname = snapshot.getString("nickname") ?: ""
                val statusString = snapshot.getString("status") ?: UserStatus.FREE.name

                val status = runCatching { UserStatus.valueOf(statusString) }
                    .getOrDefault(UserStatus.FREE)

                _me.value = UserProfile(
                    uid = user.uid,
                    name = nickname,
                    status = status
                )
            }
    }

    /** ─────────────────────────────────────────────────────────────
     *  LISTEN TO FRIENDS: users/{uid}/friends/{friendUid}
     *  ───────────────────────────────────────────────────────────── */
    private fun subscribeToFriends() {
        val user = auth.currentUser ?: return

        // remove previous listener
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

    /** ─────────────────────────────────────────────────────────────
     *  UPDATE MY STATUS
     *  ───────────────────────────────────────────────────────────── */
    fun changeStatus(newStatus: UserStatus) {
        val user = auth.currentUser ?: return

        // Update UI immediately
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
    fun removeFriend(friendUid: String) {
        val me = auth.currentUser ?: return
        val db = Firebase.firestore
        val users = db.collection("users")

        viewModelScope.launch {
            val batch = db.batch()
            val meRef = users.document(me.uid)
            val friendRef = users.document(friendUid)

            batch.delete(meRef.collection("friends").document(friendUid))
            batch.delete(friendRef.collection("friends").document(me.uid))

            batch.commit()
        }
    }

    fun blockUser(friendUid: String) {
        val me = auth.currentUser ?: return
        val db = Firebase.firestore
        val users = db.collection("users")

        viewModelScope.launch {
            val batch = db.batch()
            val meRef = users.document(me.uid)
            val friendRef = users.document(friendUid)

            // 1. Add to my blocked list
            batch.set(
                meRef.collection("blocked").document(friendUid),
                mapOf(
                    "uid" to friendUid,
                    "createdAt" to System.currentTimeMillis()
                )
            )

            // 2. Remove friendship both sides
            batch.delete(meRef.collection("friends").document(friendUid))
            batch.delete(friendRef.collection("friends").document(me.uid))

            batch.commit()
        }
    }

    /** ─────────────────────────────────────────────────────────────
     *  CLEANUP
     *  ───────────────────────────────────────────────────────────── */
    override fun onCleared() {
        super.onCleared()
        meListener?.remove()
        friendsListener?.remove()
    }
}