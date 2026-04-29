package com.example.dilo_nilo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dilo_nilo.data.models.Loan
import com.example.dilo_nilo.data.models.LoanMessage
import com.example.dilo_nilo.data.models.MessageType
import com.example.dilo_nilo.data.supabase
import com.example.dilo_nilo.repository.LoanRepository
import io.github.jan.tennert.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoanUiState(
    val loans: List<Loan> = emptyList(),
    val currentLoan: Loan? = null,
    val messages: List<LoanMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoanViewModel(
    private val repo: LoanRepository = LoanRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoanUiState())
    val uiState: StateFlow<LoanUiState> = _uiState.asStateFlow()

    fun loadLoans() {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val loans = repo.getLoansForUser(userId)
                _uiState.value = _uiState.value.copy(loans = loans, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun loadLoan(loanId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val loan = repo.getLoan(loanId)
                val messages = repo.getMessages(loanId)
                _uiState.value = _uiState.value.copy(
                    currentLoan = loan,
                    messages = messages,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun subscribeToLoanChanges(loanId: String) {
        viewModelScope.launch {
            try {
                repo.getLoanChangesFlow(loanId).collect { _ ->
                    val loan = repo.getLoan(loanId)
                    _uiState.value = _uiState.value.copy(currentLoan = loan)
                }
            } catch (e: Exception) {
                // silently ignore realtime errors
            }
        }
        viewModelScope.launch {
            try {
                repo.getMessagesFlow(loanId).collect { action ->
                    if (action is PostgresAction.Insert) {
                        val messages = repo.getMessages(loanId)
                        _uiState.value = _uiState.value.copy(messages = messages)
                    }
                }
            } catch (e: Exception) {
                // silently ignore
            }
        }
    }

    fun createLoan(
        lenderId: String,
        amount: Double,
        termMonths: Int,
        onSuccess: (String) -> Unit
    ) {
        val borrowerId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val loan = repo.createLoan(borrowerId, lenderId, amount, termMonths)
                repo.sendMessage(
                    loanId = loan.id,
                    senderId = borrowerId,
                    senderRole = "borrower",
                    text = "I am requesting a loan of $${loan.amount} for ${loan.termMonths} months.",
                    type = MessageType.REQUEST
                )
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess(loan.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun verifyVideo(loanId: String, videoBytes: ByteArray) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val url = repo.uploadVideo(userId, loanId, videoBytes)
                repo.updateLoanVideoVerified(loanId, url)
                repo.sendMessage(
                    loanId = loanId,
                    senderId = userId,
                    senderRole = "borrower",
                    text = "Video verification completed.",
                    type = MessageType.VIDEO
                )
                loadLoan(loanId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun sendMessage(loanId: String, text: String, role: String) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            try {
                val msg = repo.sendMessage(loanId, userId, role, text)
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + msg
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun approveLoan(loanId: String, paymentMethod: String, paymentProofUrl: String? = null) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repo.approveLoan(loanId, paymentMethod, paymentProofUrl)
                repo.sendMessage(
                    loanId = loanId,
                    senderId = userId,
                    senderRole = "lender",
                    text = "Loan approved. Payment method: $paymentMethod.",
                    type = MessageType.APPROVAL
                )
                loadLoan(loanId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun rejectLoan(loanId: String) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repo.rejectLoan(loanId)
                repo.sendMessage(
                    loanId = loanId,
                    senderId = userId,
                    senderRole = "lender",
                    text = "Loan request has been rejected.",
                    type = MessageType.REJECTION
                )
                loadLoan(loanId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun counterOffer(loanId: String, newAmount: Double, newTermMonths: Int) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repo.counterOffer(loanId, newAmount, newTermMonths)
                repo.sendMessage(
                    loanId = loanId,
                    senderId = userId,
                    senderRole = "lender",
                    text = "Counter offer: $$newAmount for $newTermMonths months.",
                    type = MessageType.COUNTER
                )
                loadLoan(loanId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun acceptCounter(loanId: String) {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            try {
                repo.acceptCounter(loanId)
                repo.sendMessage(
                    loanId = loanId,
                    senderId = userId,
                    senderRole = "borrower",
                    text = "Counter offer accepted.",
                    type = MessageType.ACCEPT_COUNTER
                )
                loadLoan(loanId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
