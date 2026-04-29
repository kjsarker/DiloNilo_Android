package com.example.dilo_nilo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dilo_nilo.data.models.Connection
import com.example.dilo_nilo.data.models.Profile
import com.example.dilo_nilo.data.models.SearchUserResult
import com.example.dilo_nilo.data.supabase
import com.example.dilo_nilo.repository.ConnectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConnectionUiState(
    val searchResults: List<SearchUserResult> = emptyList(),
    val connections: List<Connection> = emptyList(),
    val pendingReceived: List<Connection> = emptyList(),
    val profileCache: Map<String, Profile> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ConnectionViewModel(
    private val repo: ConnectionRepository = ConnectionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    fun searchUsers(identifier: String) {
        if (identifier.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val results = repo.searchUsers(identifier)
                _uiState.value = _uiState.value.copy(searchResults = results, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun sendConnectionRequest(receiverId: String) {
        val requesterId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repo.sendConnectionRequest(requesterId, receiverId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Connection request sent!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun loadConnections() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val connections = repo.getConnections(userId)
                val pending = repo.getPendingReceived(userId)
                val profileIds = (connections + pending)
                    .flatMap { listOf(it.requesterId, it.receiverId) }
                    .filter { it != userId }
                    .distinct()
                val profiles = profileIds.associateWith { id ->
                    repo.getProfileById(id) ?: return@associateWith null
                }.filterValues { it != null }.mapValues { it.value!! }
                _uiState.value = _uiState.value.copy(
                    connections = connections,
                    pendingReceived = pending,
                    profileCache = profiles,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun respondToRequest(connectionId: String, accept: Boolean) {
        viewModelScope.launch {
            try {
                repo.respondToRequest(connectionId, accept)
                loadConnections()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun getAcceptedConnections(userId: String, onResult: (List<Pair<Connection, Profile?>>) -> Unit) {
        viewModelScope.launch {
            try {
                val accepted = repo.getAcceptedConnections(userId)
                val withProfiles = accepted.map { conn ->
                    val otherId = if (conn.requesterId == userId) conn.receiverId else conn.requesterId
                    conn to repo.getProfileById(otherId)
                }
                onResult(withProfiles)
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null, error = null)
    }
}
