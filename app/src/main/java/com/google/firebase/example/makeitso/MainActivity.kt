package com.google.firebase.example.makeitso

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import com.google.firebase.example.makeitso.ui.home.HomeRoute
import com.google.firebase.example.makeitso.ui.home.HomeScreen
import com.google.firebase.example.makeitso.ui.profile.ProfileRoute
import com.google.firebase.example.makeitso.ui.profile.ProfileScreen
import com.google.firebase.example.makeitso.ui.signin.SignInRoute
import com.google.firebase.example.makeitso.ui.signin.SignInScreen
import com.google.firebase.example.makeitso.ui.signup.SignUpRoute
import com.google.firebase.example.makeitso.ui.signup.SignUpScreen
import com.google.firebase.example.makeitso.ui.theme.MakeItSoTheme
import com.google.firebase.example.makeitso.ui.todoitem.TodoItemRoute
import com.google.firebase.example.makeitso.ui.todoitem.TodoItemScreen
import com.google.firebase.example.makeitso.ui.todolist.TodoListRoute
import com.google.firebase.example.makeitso.ui.todolist.TodoListScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setSoftInputMode()

        setContent {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            val navController = rememberNavController()

            // Protected zone, part 1: the very first screen depends on the saved session.
            // Firebase keeps the session across app restarts, so a signed-in user lands in the
            // to-do list and everybody else on the sign-in screen.
            val startDestination: Any = remember {
                if (authRepository.currentUser != null) TodoListRoute else SignInRoute
            }

            // Protected zone, part 2: react to the session changing while the app is open.
            val currentUserId by authRepository.currentUserIdFlow.collectAsStateWithLifecycle(
                initialValue = authRepository.currentUser?.uid
            )

            LaunchedEffect(currentUserId) {
                val destination = navController.currentDestination ?: return@LaunchedEffect
                val onSignIn = destination.hasRoute<SignInRoute>()
                val onSignUp = destination.hasRoute<SignUpRoute>()

                if (currentUserId == null && !onSignIn && !onSignUp) {
                    // Signed out (or the account was deleted): leave the protected zone and
                    // clear the back stack, so "Back" can't return to it.
                    navController.navigateAndClearBackStack(SignInRoute)
                } else if (currentUserId != null && onSignIn) {
                    // Signed in from the sign-in screen (password, Google or saved credential).
                    // The sign-up screen is deliberately NOT redirected here: it first offers to
                    // save the password and then opens the to-do list itself.
                    navController.navigateAndClearBackStack(TodoListRoute)
                }
            }

            MakeItSoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable<HomeRoute> { HomeScreen(
                                openSettingsScreen = {
                                    navController.navigate(ProfileRoute) { launchSingleTop = true }
                                }
                            ) }
                            composable<TodoListRoute> { TodoListScreen(
                                openSettingsScreen = {
                                    navController.navigate(ProfileRoute) { launchSingleTop = true }
                                },
                                openTodoItemScreen = { itemId ->
                                    navController.navigate(TodoItemRoute(itemId)) { launchSingleTop = true }
                                }
                            ) }
                            composable<ProfileRoute> { ProfileScreen(
                                showErrorSnackbar = { errorMessage ->
                                    val message = getErrorMessage(errorMessage)
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            ) }
                            composable<SignInRoute> { SignInScreen(
                                openSignUpScreen = {
                                    navController.navigate(SignUpRoute) { launchSingleTop = true }
                                },
                                showErrorSnackbar = { errorMessage ->
                                    val message = getErrorMessage(errorMessage)
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            ) }
                            composable<SignUpRoute> { SignUpScreen(
                                openHomeScreen = {
                                    navController.navigateAndClearBackStack(TodoListRoute)
                                },
                                showErrorSnackbar = { errorMessage ->
                                    val message = getErrorMessage(errorMessage)
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            ) }
                            composable<TodoItemRoute> { TodoItemScreen(
                                openTodoListScreen = {
                                    navController.navigate(TodoListRoute) { launchSingleTop = true }
                                },
                                showErrorSnackbar = { errorMessage ->
                                    val message = getErrorMessage(errorMessage)
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            ) }
                        }
                    }
                }
            }
        }
    }

    private fun setSoftInputMode() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    private fun getErrorMessage(error: ErrorMessage): String {
        return when (error) {
            is ErrorMessage.StringError -> error.message
            is ErrorMessage.IdError -> this@MainActivity.getString(error.message)
        }
    }
}

/** Navigates to [route] and removes everything below it, so "Back" can't return. */
private fun NavController.navigateAndClearBackStack(route: Any) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
