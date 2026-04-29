package com.example.dilo_nilo.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dilo_nilo.data.SUPABASE_URL
import com.example.dilo_nilo.data.models.Profile
import com.example.dilo_nilo.data.supabase
import com.example.dilo_nilo.repository.AuthRepository
import io.github.jan.tennert.supabase.auth.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val profile: Profile? = null
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val sessionStatus get() = supabase.auth.sessionStatus

    init {
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    loadProfile(status.session.user?.id ?: return@collect)
                }
            }
        }
    }

    private suspend fun loadProfile(userId: String) {
        val profile = repo.getProfile(userId)
        _uiState.value = _uiState.value.copy(profile = profile)
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repo.signIn(email, password)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Sign in failed")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun signUp(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repo.signUp(fullName, email, password)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Sign up failed")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        val url = "$SUPABASE_URL/auth/v1/authorize?provider=google&redirect_to=com.example.dilo_nilo://auth"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                repo.signOut()
                _uiState.value = AuthUiState()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateProfile(fullName: String?, phone: String?) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repo.updateProfile(userId, fullName, phone)
                loadProfile(userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
