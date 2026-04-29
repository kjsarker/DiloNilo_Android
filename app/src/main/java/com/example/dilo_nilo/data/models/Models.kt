package com.example.dilo_nilo.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Profile(
    val id: String = "",
    @SerialName("full_name") val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class Loan(
    val id: String = "",
    @SerialName("borrower_id") val borrowerId: String = "",
    @SerialName("lender_id") val lenderId: String = "",
    val amount: Double = 0.0,
    @SerialName("term_months") val termMonths: Int = 0,
    val status: String = "pending",
    @SerialName("e_contract_signed") val eContractSigned: Boolean = false,
    @SerialName("video_verified") val videoVerified: Boolean = false,
    @SerialName("video_url") val videoUrl: String? = null,
    @SerialName("disbursed_at") val disbursedAt: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("payment_proof_url") val paymentProofUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class LoanMessage(
    val id: String = "",
    @SerialName("loan_id") val loanId: String = "",
    @SerialName("sender_id") val senderId: String = "",
    @SerialName("sender_role") val senderRole: String = "",
    val text: String = "",
    val type: String = "info",
    val metadata: JsonObject? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Connection(
    val id: String = "",
    @SerialName("requester_id") val requesterId: String = "",
    @SerialName("receiver_id") val receiverId: String = "",
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class SearchUserResult(
    val id: String = "",
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("masked_email") val maskedEmail: String? = null,
    @SerialName("masked_phone") val maskedPhone: String? = null
)

object LoanStatus {
    const val PENDING = "pending"
    const val APPROVED = "approved"
    const val ACTIVE = "active"
    const val DUE_SOON = "due_soon"
    const val OVERDUE = "overdue"
    const val COMPLETED = "completed"
    const val REJECTED = "rejected"
    const val COUNTER_OFFERED = "counter_offered"
}

object MessageType {
    const val REQUEST = "request"
    const val APPROVAL = "approval"
    const val REJECTION = "rejection"
    const val COUNTER = "counter"
    const val ACCEPT_COUNTER = "accept_counter"
    const val INFO = "info"
    const val VIDEO = "video"
    const val SYSTEM = "system"
}

object ConnectionStatus {
    const val PENDING = "pending"
    const val ACCEPTED = "accepted"
    const val REJECTED = "rejected"
}
