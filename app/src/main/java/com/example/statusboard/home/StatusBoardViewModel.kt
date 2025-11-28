package com.example.statusboard.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.statusboard.data.UserRepository
import com.example.statusboard.domain.model.UserProfile
import com.example.statusboard.domain.model.UserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatusBoardViewModel(
    private val repo: UserRepository = UserRepository()
) : ViewModel() {

    private val _me = MutableStateFlow<UserProfile?>(null)
    val me: StateFlow<UserProfile?> = _me.asStateFlow()

    init {
        // Start listening to Firestore
        viewModelScope.launch {
            repo.observeCurrentUser().collect { profile ->
                _me.value = profile
            }
        }
    }

    fun changeStatus(status: UserStatus) {
        repo.updateStatus(status)
        // UI will update when Firestore document updates from listener
    }
}