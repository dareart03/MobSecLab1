package com.google.firebase.example.makeitso.ui.profile

import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.data.model.UserProfile
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * After sign-out or account deletion nothing navigates from here: MainActivity sees the user
 * disappear and sends the app to the sign-in screen with an empty back stack.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : MainViewModel() {
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?>
        get() = _profile.asStateFlow()

    fun loadProfile(showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching(showErrorSnackbar) {
            _profile.value = authRepository.getUserProfile()
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun deleteAccount(showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching(showErrorSnackbar) {
            authRepository.deleteAccount()
        }
    }
}
