package ci.nsu.mobile.main.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
    data class GroupsLoaded(val groups: List<GroupDto>) : RegisterUiState()
}

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun loadGroups() {
        viewModelScope.launch {
            val result = repository.getGroups()
            if (result.isSuccess && result.getOrNull() != null) {
                _uiState.value = RegisterUiState.GroupsLoaded(result.getOrNull()!!)
            } else {
                _uiState.value = RegisterUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load groups")
            }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        middleName: String,
        birthDate: String,
        gender: String,
        groupId: Int,
        login: String,
        password: String,
        email: String,
        phoneNumber: String
    ) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            val person = PersonDto(
                firstName = firstName,
                lastName = lastName,
                middleName = middleName,
                birthDate = birthDate,
                gender = gender,
                groupId = groupId
            )

            val request = RegisterRequest(
                login = login,
                password = password,
                email = email,
                phoneNumber = phoneNumber,
                roleId = 1,
                authAllowed = true,
                person = person
            )

            val result = repository.register(request)
            _uiState.value = when {
                result.isSuccess -> RegisterUiState.Success
                else -> RegisterUiState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }
}