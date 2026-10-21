package com.google.firebase.example.makeitso.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.example.makeitso.data.credential.CredentialAuthClient

/** Creates a [CredentialAuthClient] bound to the current Activity context. */
@Composable
fun rememberCredentialAuthClient(): CredentialAuthClient {
    val context = LocalContext.current
    return remember(context) { CredentialAuthClient(context) }
}
