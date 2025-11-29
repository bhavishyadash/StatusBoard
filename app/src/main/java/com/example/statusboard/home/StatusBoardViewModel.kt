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

    private val _friends = MutableStateFlow<List<UserProfile>>(emptyList())
    val friends: StateFlow<List<UserProfile>> = _friends.asStateFlow()

    init {
        // listen to current user
        viewModelScope.launch {
            repo.observeCurrentUser().collect { profile ->
                _me.value = profile
            }
        }

        // listen to friends list
        viewModelScope.launch {
            repo.observeFriends().collect { list ->
                _friends.value = list
            }
        }
    }

    fun changeStatus(status: UserStatus) {
        repo.updateStatus(status)
    }
}