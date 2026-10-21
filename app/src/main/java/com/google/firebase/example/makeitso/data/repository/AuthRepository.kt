package com.google.firebase.example.makeitso.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.example.makeitso.data.datasource.AuthRemoteDataSource
import com.google.firebase.example.makeitso.data.model.SignInMethod
import com.google.firebase.example.makeitso.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) {
    // A getter (not a stored value), so it always reflects the current session.
    val currentUser: FirebaseUser?
        get() = authRemoteDataSource.currentUser

    val currentUserIdFlow: Flow<String?> = authRemoteDataSource.currentUserIdFlow

    /** Photo URL of the signed-in account (null when it has none, e.g. email sign-up). */
    val currentPhotoUrl: String?
        get() {
            val user = currentUser ?: return null
            return (user.photoUrl ?: user.providerData.firstNotNullOfOrNull { it.photoUrl })
                ?.toString()
        }

    suspend fun signIn(email: String, password: String) {
        authRemoteDataSource.signIn(email, password)
    }

    suspend fun signUp(email: String, password: String) {
        authRemoteDataSource.signUp(email, password)
    }

    suspend fun signInWithGoogle(idToken: String) {
        authRemoteDataSource.signInWithGoogle(idToken)
    }

    fun signOut() {
        authRemoteDataSource.signOut()
    }

    suspend fun deleteAccount() {
        authRemoteDataSource.deleteAccount()
    }

    /** Builds the data shown on the profile screen, or null when nobody is signed in. */
    suspend fun getUserProfile(): UserProfile? {
        val user = currentUser ?: return null
        val linkedProviders = user.providerData
            .map { it.providerId }
            .filter { it != FIREBASE_PROVIDER_ID }

        // Prefer the provider of the current session; fall back to the linked providers.
        val sessionProvider = authRemoteDataSource.getSignInProvider()
        val effectiveProvider = sessionProvider ?: linkedProviders.firstOrNull()

        val method = when (effectiveProvider) {
            PASSWORD_PROVIDER_ID -> SignInMethod.EMAIL_PASSWORD
            GOOGLE_PROVIDER_ID -> SignInMethod.GOOGLE
            else -> SignInMethod.OTHER
        }

        // The Firebase user's own email/name may be empty while a linked provider has them.
        val email = user.email ?: user.providerData.firstNotNullOfOrNull { it.email }
        val displayName = user.displayName
            ?: user.providerData.firstNotNullOfOrNull { it.displayName }
        val photoUrl = (user.photoUrl ?: user.providerData.firstNotNullOfOrNull { it.photoUrl })
            ?.toString()

        return UserProfile(
            uid = user.uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            emailVerified = user.isEmailVerified,
            signInMethod = method,
            signInProviderId = effectiveProvider,
            linkedProviders = linkedProviders,
            createdAtMillis = user.metadata?.creationTimestamp,
            lastSignInAtMillis = user.metadata?.lastSignInTimestamp
        )
    }

    companion object {
        private const val FIREBASE_PROVIDER_ID = "firebase"
        private const val PASSWORD_PROVIDER_ID = "password"
        private const val GOOGLE_PROVIDER_ID = "google.com"
    }
}