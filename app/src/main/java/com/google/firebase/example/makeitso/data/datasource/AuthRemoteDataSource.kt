package com.google.firebase.example.makeitso.data.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(private val auth: FirebaseAuth) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val currentUserIdFlow: Flow<String?>
        get() = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { _ -> this.trySend(currentUser?.uid) }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    /** Signs in to Firebase with the Google ID token obtained through Credential Manager. */
    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    /**
     * Returns the provider id the CURRENT SESSION was opened with (e.g. "password" or
     * "google.com"), read from the Firebase ID token. Falls back to null when it can't be read
     * (for example, offline with an expired token).
     */
    suspend fun getSignInProvider(): String? {
        val user = auth.currentUser ?: return null
        return try {
            user.getIdToken(false).await().signInProvider
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun deleteAccount() {
        auth.currentUser!!.delete().await()
    }
}
