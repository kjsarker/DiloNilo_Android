[README.md](https://github.com/user-attachments/files/27204298/README.md)
# DiloNilo — Android Peer-to-Peer Lending App

DiloNilo is an Android app that mirrors the [trustify-vid-agree](https://github.com/kjsarker/trustify-vid-agree) web app — a peer-to-peer loan platform with video verification and real-time chat. Both apps share the same Supabase backend.

## Features

- **Email & Google authentication** with deep link OAuth callback support
- **Borrow flow** — search for a lender, submit a loan request, sign an e-contract, record a video verification
- **Lend flow** — review incoming requests, approve / reject / counter-offer, upload payment proof
- **Real-time chat** between borrower and lender on each loan
- **Connections** — send, accept, and manage trusted contacts
- **Profile management** — view and edit name, phone, and avatar
- **Live loan status tracking** via Supabase Realtime subscriptions

## Screenshots

> Add screenshots here

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + Repository) |
| Backend | Supabase (Auth, PostgREST, Realtime, Storage) |
| HTTP client | Ktor 3 + OkHttp engine |
| Navigation | Jetpack Navigation Compose |
| Image loading | Coil |
| Camera / Video | CameraX |
| Serialization | kotlinx.serialization |

## Project Structure

```
app/src/main/java/com/example/dilo_nilo/
├── data/
│   ├── SupabaseClient.kt       # Supabase client singleton
│   └── models/Models.kt        # Data classes (Loan, Profile, Connection, …)
├── repository/
│   ├── AuthRepository.kt
│   ├── LoanRepository.kt
│   └── ConnectionRepository.kt
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── LoanViewModel.kt
│   └── ConnectionViewModel.kt
├── ui/
│   ├── navigation/NavGraph.kt
│   ├── screens/                # One file per screen
│   ├── components/             # BottomNav, StatusPill
│   └── theme/                  # Color, Type, Theme
└── MainActivity.kt
```

## Supabase Schema

**Tables:** `profiles`, `loans`, `loan_messages`, `connections`, `user_roles`

**Storage buckets:** `loan-videos`, `payment-proofs`

**Database function:** `search_user_by_identifier(identifier TEXT)`

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 26+
- A Supabase project (or use the existing one — credentials are in `SupabaseClient.kt`)

### Clone & Run

```bash
git clone https://github.com/kjsarker/DiloNilo_Android.git
cd DiloNilo_Android
```

Open the project in Android Studio, wait for Gradle sync, then run on a device or emulator (API 26+).

### Deep Link Setup (Google OAuth)

The app uses the custom scheme `com.example.dilo_nilo://auth` for OAuth redirects. This is already registered in `AndroidManifest.xml`. Make sure the same redirect URL is added to your Supabase project under **Authentication → URL Configuration → Redirect URLs**.

## Related Repository

Web app: [kjsarker/trustify-vid-agree](https://github.com/kjsarker/trustify-vid-agree)
