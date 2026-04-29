package com.example.dilo_nilo.repository

import com.example.dilo_nilo.data.models.Connection
import com.example.dilo_nilo.data.models.ConnectionStatus
import com.example.dilo_nilo.data.models.Profile
import com.example.dilo_nilo.data.models.SearchUserResult
import com.example.dilo_nilo.data.supabase
import io.github.jan.tennert.supabase.postgrest.from
import io.github.jan.tennert.supabase.postgrest.rpc
import java.util.UUID

class ConnectionRepository {

    suspend fun searchUsers(identifier: String): List<SearchUserResult> {
        return try {
            supabase.postgrest.rpc(
                "search_user_by_identifier",
                buildMap { put("identifier", identifier) }
            ).decodeList<SearchUserResult>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendConnectionRequest(requesterId: String, receiverId: String) {
        val conn = Connection(
            id = UUID.randomUUID().toString(),
            requesterId = requesterId,
            receiverId = receiverId,
            status = ConnectionStatus.PENDING
        )
        supabase.from("connections").insert(conn)
    }

    suspend fun getConnections(userId: String): List<Connection> {
        return try {
            val sent = supabase.from("connections")
                .select { filter { eq("requester_id", userId) } }
                .decodeList<Connection>()
            val received = supabase.from("connections")
                .select { filter { eq("receiver_id", userId) } }
                .decodeList<Connection>()
            (sent + received).distinctBy { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPendingReceived(userId: String): List<Connection> {
        return try {
            supabase.from("connections")
                .select {
                    filter {
                        eq("receiver_id", userId)
                        eq("status", ConnectionStatus.PENDING)
                    }
                }
                .decodeList<Connection>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAcceptedConnections(userId: String): List<Connection> {
        return try {
            val sent = supabase.from("connections")
                .select {
                    filter {
                        eq("requester_id", userId)
                        eq("status", ConnectionStatus.ACCEPTED)
                    }
                }
                .decodeList<Connection>()
            val received = supabase.from("connections")
                .select {
                    filter {
                        eq("receiver_id", userId)
                        eq("status", ConnectionStatus.ACCEPTED)
                    }
                }
                .decodeList<Connection>()
            (sent + received).distinctBy { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun respondToRequest(connectionId: String, accept: Boolean) {
        val status = if (accept) ConnectionStatus.ACCEPTED else ConnectionStatus.REJECTED
        supabase.from("connections").update({
            set("status", status)
        }) {
            filter { eq("id", connectionId) }
        }
    }

    suspend fun getProfileById(userId: String): Profile? {
        return try {
            supabase.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            null
        }
    }
}
