package com.google.firebase.example.makeitso.ui.profile

import android.graphics.BitmapFactory
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.example.makeitso.R
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.data.model.SignInMethod
import com.google.firebase.example.makeitso.data.model.UserProfile
import com.google.firebase.example.makeitso.ui.shared.CenterTopAppBar
import com.google.firebase.example.makeitso.ui.shared.LoadingIndicator
import com.google.firebase.example.makeitso.ui.shared.StandardButton
import com.google.firebase.example.makeitso.ui.shared.rememberCredentialAuthClient
import com.google.firebase.example.makeitso.ui.theme.DarkBlue
import com.google.firebase.example.makeitso.ui.theme.DarkGrey
import com.google.firebase.example.makeitso.ui.theme.LightRed
import com.google.firebase.example.makeitso.ui.theme.MakeItSoTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.net.URL
import java.text.DateFormat
import java.util.Date

@Serializable
object ProfileRoute

@Composable
fun ProfileScreen(
    showErrorSnackbar: (ErrorMessage) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val credentialClient = rememberCredentialAuthClient()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadProfile(showErrorSnackbar)
    }

    ProfileScreenContent(
        profile = profile,
        signOut = {
            scope.launch {
                // Tell the credential providers about the sign-out first, then end the session.
                // MainActivity then moves to the sign-in screen with an empty back stack.
                credentialClient.clearCredentialState()
                viewModel.signOut()
            }
        },
        deleteAccount = { viewModel.deleteAccount(showErrorSnackbar) }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileScreenContent(
    profile: UserProfile?,
    signOut: () -> Unit,
    deleteAccount: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = stringResource(R.string.profile),
                scrollBehavior = scrollBehavior,
                showBackButton = true
            )
        },
    ) { innerPadding ->
        if (profile == null) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 16.dp
                    )
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfilePhoto(
                    photoUrl = profile.photoUrl,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                val notAvailable = stringResource(R.string.not_available)

                ProfileRow(R.string.sign_in_method, signInMethodLabel(profile))
                ProfileRow(R.string.profile_email, profile.email ?: notAvailable)
                ProfileRow(
                    R.string.profile_display_name,
                    profile.displayName?.takeIf { it.isNotBlank() } ?: notAvailable
                )
                ProfileRow(R.string.profile_user_id, profile.uid)
                ProfileRow(
                    R.string.profile_email_verified,
                    stringResource(if (profile.emailVerified) R.string.yes else R.string.no)
                )
                ProfileRow(
                    R.string.profile_linked_providers,
                    profile.linkedProviders.joinToString().ifBlank { notAvailable }
                )
                ProfileRow(
                    R.string.profile_created,
                    formatDateTime(profile.createdAtMillis, notAvailable)
                )
                ProfileRow(
                    R.string.profile_last_sign_in,
                    formatDateTime(profile.lastSignInAtMillis, notAvailable)
                )

                Spacer(Modifier.size(12.dp))

                StandardButton(
                    label = R.string.sign_out,
                    onButtonClick = signOut
                )

                DeleteAccountButton(deleteAccount)
            }
        }
    }
}

@Composable
private fun signInMethodLabel(profile: UserProfile): String {
    return when (profile.signInMethod) {
        SignInMethod.EMAIL_PASSWORD -> stringResource(R.string.sign_in_method_email)
        SignInMethod.GOOGLE -> stringResource(R.string.sign_in_method_google)
        SignInMethod.OTHER -> profile.signInProviderId ?: stringResource(R.string.not_available)
    }
}

private fun formatDateTime(millis: Long?, fallback: String): String {
    if (millis == null || millis <= 0L) return fallback
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(Date(millis))
}

@Composable
private fun ProfileRow(@StringRes label: Int, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            // Follows the theme (light text in dark mode) instead of a fixed dark grey.
            color = LocalContentColor.current.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/** Loads the account photo from its URL; shows a placeholder icon while loading or if none. */
@Composable
private fun ProfilePhoto(photoUrl: String?, modifier: Modifier = Modifier) {
    val photo by produceState<ImageBitmap?>(initialValue = null, photoUrl) {
        value = if (photoUrl.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                try {
                    URL(photoUrl).openStream().use { BitmapFactory.decodeStream(it) }
                        ?.asImageBitmap()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    val bitmap = photo
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Profile photo",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(96.dp)
                .clip(CircleShape)
        )
    } else {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            tint = LocalContentColor.current,
            contentDescription = "Profile picture placeholder",
            modifier = modifier.size(96.dp)
        )
    }
}

@Composable
fun DeleteAccountButton(deleteAccount: () -> Unit) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    StandardButton(
        label = R.string.delete_account,
        onButtonClick = {
            showDeleteAccountDialog = true
        }
    )

    if (showDeleteAccountDialog) {
        AlertDialog(
            containerColor = LightRed,
            textContentColor = DarkBlue,
            titleContentColor = DarkBlue,
            title = { Text(stringResource(R.string.delete_account_title)) },
            text = { Text(stringResource(R.string.delete_account_description)) },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    colors = getDialogButtonColors()
                ) {
                    Text(text = stringResource(R.string.cancel), fontSize = 16.sp)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        deleteAccount()
                    },
                    colors = getDialogButtonColors()
                ) {
                    Text(text = stringResource(R.string.delete), fontSize = 16.sp)
                }
            },
            onDismissRequest = { showDeleteAccountDialog = false }
        )
    }
}

private fun getDialogButtonColors(): ButtonColors {
    return ButtonColors(
        containerColor = LightRed,
        contentColor = DarkBlue,
        disabledContainerColor = LightRed,
        disabledContentColor = DarkGrey
    )
}

@Composable
@Preview(showSystemUi = true)
fun ProfileScreenPreview() {
    MakeItSoTheme(darkTheme = true) {
        ProfileScreenContent(
            profile = UserProfile(
                uid = "abc123",
                email = "user@example.com",
                displayName = "Example User",
                photoUrl = null,
                emailVerified = false,
                signInMethod = SignInMethod.GOOGLE,
                signInProviderId = "google.com",
                linkedProviders = listOf("google.com"),
                createdAtMillis = 1_700_000_000_000,
                lastSignInAtMillis = 1_700_000_100_000
            ),
            signOut = {},
            deleteAccount = {}
        )
    }
}