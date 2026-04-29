package com.example.dilo_nilo.repository

import com.example.dilo_nilo.data.models.Loan
import com.example.dilo_nilo.data.models.LoanMessage
import com.example.dilo_nilo.data.models.LoanStatus
import com.example.dilo_nilo.data.models.MessageType
import com.example.dilo_nilo.data.supabase
import io.github.jan.tennert.supabase.postgrest.from
import io.github.jan.tennert.supabase.realtime.channel
import io.github.jan.tennert.supabase.realtime.postgresChangeFlow
import io.github.jan.tennert.supabase.realtime.PostgresAction
import io.github.jan.tennert.supabase.realtime.realtime
import io.github.jan.tennert.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

class LoanRepository {

    suspend fun getLoansForUser(userId: String): List<Loan> {
        return try {
            val asBorrower = supabase.from("loans")
                .select { filter { eq("borrower_id", userId) } }
                .decodeList<Loan>()
            val asLender = supabase.from("loans")
                .select { filter { eq("lender_id", userId) } }
                .decodeList<Loan>()
            (asBorrower + asLender).distinctBy { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLoan(loanId: String): Loan? {
        return try {
            supabase.from("loans")
                .select { filter { eq("id", loanId) } }
                .decodeSingleOrNull<Loan>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createLoan(
        borrowerId: String,
        lenderId: String,
        amount: Double,
        termMonths: Int
    ): Loan {
        val loan = Loan(
            id = UUID.randomUUID().toString(),
            borrowerId = borrowerId,
            lenderId = lenderId,
            amount = amount,
            termMonths = termMonths,
            status = LoanStatus.PENDING,
            eContractSigned = true
        )
        supabase.from("loans").insert(loan)
        return loan
    }

    suspend fun updateLoanVideoVerified(loanId: String, videoUrl: String) {
        supabase.from("loans").update({
            set("video_verified", true)
            set("video_url", videoUrl)
        }) {
            filter { eq("id", loanId) }
        }
    }

    suspend fun approveLoan(loanId: String, paymentMethod: String, paymentProofUrl: String?) {
        supabase.from("loans").update({
            set("status", LoanStatus.ACTIVE)
            set("payment_method", paymentMethod)
            paymentProofUrl?.let { set("payment_proof_url", it) }
            set("disbursed_at", java.time.Instant.now().toString())
        }) {
            filter { eq("id", loanId) }
        }
    }

    suspend fun rejectLoan(loanId: String) {
        supabase.from("loans").update({
            set("status", LoanStatus.REJECTED)
        }) {
            filter { eq("id", loanId) }
        }
    }

    suspend fun counterOffer(loanId: String, newAmount: Double, newTermMonths: Int) {
        supabase.from("loans").update({
            set("amount", newAmount)
            set("term_months", newTermMonths)
            set("status", LoanStatus.COUNTER_OFFERED)
        }) {
            filter { eq("id", loanId) }
        }
    }

    suspend fun acceptCounter(loanId: String) {
        supabase.from("loans").update({
            set("status", LoanStatus.PENDING)
        }) {
            filter { eq("id", loanId) }
        }
    }

    suspend fun markCompleted(loanId: String) {
        supabase.from("loans").update({
            set("status", LoanStatus.COMPLETED)
        }) {
            filter { eq("id", loanId) }
        }
    }

    suspend fun getMessages(loanId: String): List<LoanMessage> {
        return try {
            supabase.from("loan_messages")
                .select { filter { eq("loan_id", loanId) } }
                .decodeList<LoanMessage>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendMessage(
        loanId: String,
        senderId: String,
        senderRole: String,
        text: String,
        type: String = MessageType.INFO
    ): LoanMessage {
        val msg = LoanMessage(
            id = UUID.randomUUID().toString(),
            loanId = loanId,
            senderId = senderId,
            senderRole = senderRole,
            text = text,
            type = type
        )
        supabase.from("loan_messages").insert(msg)
        return msg
    }

    suspend fun uploadVideo(userId: String, loanId: String, videoBytes: ByteArray): String {
        val path = "$userId/$loanId.mp4"
        supabase.storage["loan-videos"].upload(path, videoBytes, upsert = true)
        return supabase.storage["loan-videos"].publicUrl(path)
    }

    suspend fun uploadPaymentProof(userId: String, loanId: String, imageBytes: ByteArray): String {
        val path = "$userId/${loanId}_proof.jpg"
        supabase.storage["payment-proofs"].upload(path, imageBytes, upsert = true)
        return supabase.storage["payment-proofs"].publicUrl(path)
    }

    fun getLoanChangesFlow(loanId: String): Flow<PostgresAction> {
        val channel = supabase.realtime.channel("loan-$loanId")
        return channel.postgresChangeFlow(schema = "public") {
            table = "loans"
            filter = "id=eq.$loanId"
        }
    }

    fun getMessagesFlow(loanId: String): Flow<PostgresAction> {
        val channel = supabase.realtime.channel("messages-$loanId")
        return channel.postgresChangeFlow(schema = "public") {
            table = "loan_messages"
            filter = "loan_id=eq.$loanId"
        }
    }
}
