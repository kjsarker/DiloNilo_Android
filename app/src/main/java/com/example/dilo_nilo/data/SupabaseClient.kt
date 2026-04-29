package com.example.dilo_nilo.data

import io.github.jan.tennert.supabase.auth.Auth
import io.github.jan.tennert.supabase.createSupabaseClient
import io.github.jan.tennert.supabase.postgrest.Postgrest
import io.github.jan.tennert.supabase.realtime.Realtime
import io.github.jan.tennert.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp

const val SUPABASE_URL = "https://owdjxdbvzjcayffapqgv.supabase.co"
const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im93ZGp4ZGJ2empjYXlmZmFwcWd2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzYzMTg2NDcsImV4cCI6MjA5MTg5NDY0N30.pwBOlUSjabSpCEXII3wFyPBsWJHStjVxSoT1H6bUjoo"

val supabase = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_ANON_KEY
) {
    install(Auth) {
        scheme = "com.example.dilo_nilo"
        host = "auth"
    }
    install(Postgrest)
    install(Realtime)
    install(Storage)
    httpEngine = OkHttp.create()
}
