package com.google.firebase.example.makeitso.ui.signin

import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Navigation after a successful sign-in is NOT done here: MainActivity watches the auth state
 * and moves to the protected zone (clearing the back stack) as soon as a user appears.
 */
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : MainViewModel() {
    fun signIn(
        email: String,
        password: String,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        launchCatching(showErrorSnackbar) {
            authRepository.signIn(email, password)
        }
    }

    fun signInWithGoogle(
        idToken: String,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        launchCatching(showErrorSnackbar) {
            authRepository.signInWithGoogle(idToken)
        }
    }
}
