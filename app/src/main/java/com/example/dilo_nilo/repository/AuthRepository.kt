package com.example.dilo_nilo.repository

import com.example.dilo_nilo.data.models.Profile
import com.example.dilo_nilo.data.supabase
import io.github.jan.tennert.supabase.auth.auth
import io.github.jan.tennert.supabase.auth.providers.Email
import io.github.jan.tennert.supabase.auth.providers.Google
import io.github.jan.tennert.supabase.auth.user.UserInfo
import io.github.jan.tennert.supabase.postgrest.from
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {

    val sessionStatus get() = supabase.auth.sessionStatus

    fun currentUser(): UserInfo? = supabase.auth.currentUserOrNull()

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(fullName: String, email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("full_name", fullName)
            }
        }
    }

    suspend fun signInWithGoogle() {
        supabase.auth.signInWith(Google)
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    suspend fun getProfile(userId: String): Profile? {
        return try {
            supabase.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(userId: String, fullName: String?, phone: String?) {
        supabase.from("profiles").update({
            fullName?.let { set("full_name", it) }
            phone?.let { set("phone", it) }
        }) {
            filter { eq("id", userId) }
        }
    }
}
