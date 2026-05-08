package ci.nsu.mobile.main.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UsersUiState {
    object Idle : UsersUiState()
    object Loading : UsersUiState()
    data class Success(val users: List<UserDto>) : UsersUiState()
    data class Error(val message: String) : UsersUiState()
}

class UsersViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UsersUiState>(UsersUiState.Idle)
    val uiState: StateFlow<UsersUiState> = _uiState

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UsersUiState.Loading
            val result = repository.getUsers()
            _uiState.value = when {
                result.isSuccess && result.getOrNull() != null -> UsersUiState.Success(result.getOrNull()!!)
                else -> UsersUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load users")
            }
        }
    }
}