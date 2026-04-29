package com.example.dilo_nilo

import android.app.Application

class DiloNiloApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Supabase client is initialized as a top-level singleton in SupabaseClient.kt
    }
}
