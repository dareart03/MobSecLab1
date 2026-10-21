package com.google.firebase.example.makeitso.data.model

/** How the user signed in. Mapped to a readable label in the UI. */
enum class SignInMethod {
    EMAIL_PASSWORD,
    GOOGLE,
    OTHER
}

/** Snapshot of the authenticated user shown on the profile screen. */
data class UserProfile(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val emailVerified: Boolean,
    val signInMethod: SignInMethod,
    /** Raw provider id of the current session, e.g. "password" or "google.com". */
    val signInProviderId: String?,
    /** All providers linked to the account, e.g. ["password", "google.com"]. */
    val linkedProviders: List<String>,
    val createdAtMillis: Long?,
    val lastSignInAtMillis: Long?
)
