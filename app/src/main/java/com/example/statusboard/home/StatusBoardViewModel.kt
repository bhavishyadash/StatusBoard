package com.example.statusboard.home

import androidx.lifecycle.ViewModel
import com.example.statusboard.domain.model.UserProfile
import com.example.statusboard.domain.model.UserStatus
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StatusBoardViewModel : ViewModel() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Root users collection
    private val usersRef = firestore.collection("users")

    // ----- UI State -----
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _friends = MutableStateFlow<List<UserProfile>>(emptyList())
    val friends: StateFlow<List<UserProfile>> = _friends.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        listenToCurrentUser()
        listenToFriends()
    }

    // ---------------------------
    // Firestore listeners
    // ---------------------------

    private fun listenToCurrentUser() {
        val uid = auth.currentUser?.uid ?: run {
            _errorMessage.value = "Not authenticated"
            return
        }

        usersRef.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = error.message
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    _userProfile.value = null
                    return@addSnapshotListener
                }

                val nickname = snapshot.getString("nickname") ?: ""
                val statusStr = snapshot.getString("status") ?: UserStatus.FREE.name
                val status = runCatching { UserStatus.valueOf(statusStr) }
                    .getOrDefault(UserStatus.FREE)

                val calendarEnabled = snapshot.getBoolean("calendarAutoStatusEnabled") ?: false

                _userProfile.value = UserProfile(
                    uid = uid,
                    nickname = nickname,
                    status = status,
                    calendarAutoStatusEnabled = calendarEnabled
                )
            }
    }

    private fun listenToFriends() {
        val uid = auth.currentUser?.uid ?: return

        // expects: users/{uid}/friends/{friendUid} with at least { uid: friendUid }
        usersRef.document(uid)
            .collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = error.message
                    return@addSnapshotListener
                }

                val friendUids = snapshot?.documents
                    ?.mapNotNull { it.getString("uid") ?: it.id }
                    ?: emptyList()

                if (friendUids.isEmpty()) {
                    _friends.value = emptyList()
                    return@addSnapshotListener
                }

                // fetch friend profiles (simple approach)
                usersRef.whereIn("__name__", friendUids.take(10)) // Firestore whereIn limit is 10
                    .get()
                    .addOnSuccessListener { friendsSnap ->
                        val list = friendsSnap.documents.map { doc ->
                            val nick = doc.getString("nickname") ?: "Friend"
                            val s = runCatching {
                                UserStatus.valueOf(doc.getString("status") ?: UserStatus.FREE.name)
                            }.getOrDefault(UserStatus.FREE)

                            UserProfile(
                                uid = doc.id,
                                nickname = nick,
                                status = s,
                                calendarAutoStatusEnabled = doc.getBoolean("calendarAutoStatusEnabled") ?: false
                            )
                        }
                        _friends.value = list
                    }
            }
    }

    // ---------------------------
    // Actions
    // ---------------------------

    /**
     * Manual status change should override any automation.
     */
    fun changeStatus(status: UserStatus) {
        val uid = auth.currentUser?.uid ?: return

        usersRef.document(uid).update(
            mapOf(
                "status" to status.name,
                "autoStatus" to false,
                "statusExpiresAt" to null
            )
        )
    }

    /**
     * Toggles the calendar auto-status feature flag.
     */
    fun toggleCalendarAutoStatus(enabled: Boolean) {
        val uid = auth.currentUser?.uid ?: return

        usersRef.document(uid).update(
            "calendarAutoStatusEnabled", enabled
        )
    }

    fun removeFriend(friendUid: String) {
        val uid = auth.currentUser?.uid ?: return

        // Remove from my friends list
        usersRef.document(uid)
            .collection("friends")
            .document(friendUid)
            .delete()

        // Optional: remove me from their list (comment out if you don't store both sides)
        usersRef.document(friendUid)
            .collection("friends")
            .document(uid)
            .delete()
    }

    fun blockUser(friendUid: String) {
        val uid = auth.currentUser?.uid ?: return

        val blockedRef = usersRef.document(uid)
            .collection("blocked")
            .document(friendUid)

        blockedRef.set(
            mapOf(
                "uid" to friendUid,
                "blockedAt" to Timestamp.now()
            )
        ).addOnSuccessListener {
            // also remove from friends
            removeFriend(friendUid)
        }
    }
}