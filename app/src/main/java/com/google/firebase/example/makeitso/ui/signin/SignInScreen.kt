package com.google.firebase.example.makeitso.ui.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.example.makeitso.R
import com.google.firebase.example.makeitso.data.credential.CredentialResult
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.ui.shared.AuthWithGoogleButton
import com.google.firebase.example.makeitso.ui.shared.StandardButton
import com.google.firebase.example.makeitso.ui.shared.rememberCredentialAuthClient
import com.google.firebase.example.makeitso.ui.theme.DarkBlue
import com.google.firebase.example.makeitso.ui.theme.MakeItSoTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object SignInRoute

@Composable
fun SignInScreen(
    openSignUpScreen: () -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val credentialClient = rememberCredentialAuthClient()
    val scope = rememberCoroutineScope()
    // rememberSaveable: don't pop the system dialog again after a configuration change.
    var promptShown by rememberSaveable { mutableStateOf(false) }

    fun handle(result: CredentialResult) {
        when (result) {
            is CredentialResult.Password ->
                viewModel.signIn(result.email, result.password, showErrorSnackbar)

            is CredentialResult.Google ->
                viewModel.signInWithGoogle(result.idToken, showErrorSnackbar)

            is CredentialResult.Error ->
                showErrorSnackbar(ErrorMessage.StringError(result.message))

            CredentialResult.Dismissed -> Unit
        }
    }

    // One system dialog with the saved password(s) AND the Google account(s).
    LaunchedEffect(Unit) {
        if (!promptShown) {
            promptShown = true
            handle(credentialClient.requestSavedCredential())
        }
    }

    SignInScreenContent(
        openSignUpScreen = openSignUpScreen,
        signIn = viewModel::signIn,
        signInWithGoogle = {
            scope.launch { handle(credentialClient.requestGoogleAccount()) }
        },
        showErrorSnackbar = showErrorSnackbar
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SignInScreenContent(
    openSignUpScreen: () -> Unit,
    signIn: (String, String, (ErrorMessage) -> Unit) -> Unit,
    signInWithGoogle: () -> Unit,
    showErrorSnackbar: (ErrorMessage) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        ConstraintLayout(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val (appLogo, form, signUpText) = createRefs()

            Column(
                modifier = Modifier
                    .constrainAs(appLogo) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.size(24.dp))

                Image(
                    modifier = Modifier.size(88.dp),
                    painter = painterResource(id = R.mipmap.ic_launcher_round),
                    contentDescription = "App logo"
                )

                Spacer(Modifier.size(24.dp))
            }

            Column(
                modifier = Modifier
                    .constrainAs(form) {
                        top.linkTo(appLogo.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.size(24.dp))

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email)) }
                )

                Spacer(Modifier.size(16.dp))

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(Modifier.size(32.dp))

                StandardButton(
                    label = R.string.sign_in_with_email,
                    onButtonClick = {
                        signIn(email, password, showErrorSnackbar)
                    }
                )

                Spacer(Modifier.size(16.dp))

                AuthWithGoogleButton(R.string.sign_in_with_google) { signInWithGoogle() }
            }

            Column(
                modifier = Modifier
                    .constrainAs(signUpText) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = openSignUpScreen) {
                    Text(
                        text = stringResource(R.string.sign_up_text),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = DarkBlue
                    )
                }

                Spacer(Modifier.size(24.dp))
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun SignInScreenPreview() {
    MakeItSoTheme(darkTheme = true) {
        SignInScreenContent(
            openSignUpScreen = {},
            signIn = { _, _, _ -> },
            signInWithGoogle = {},
            showErrorSnackbar = {}
        )
    }
}