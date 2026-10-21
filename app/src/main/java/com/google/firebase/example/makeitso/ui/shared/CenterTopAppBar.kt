package com.google.firebase.example.makeitso.ui.shared

import android.os.Build
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.google.firebase.example.makeitso.ui.theme.DarkGrey
import com.google.firebase.example.makeitso.ui.theme.LightYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterTopAppBar(
    title: String,
    icon: ImageVector,
    iconDescription: String,
    action: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    showBackButton: Boolean = false
) {
    CenterAlignedTopAppBar(
        colors = appBarColors(),
        title = {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = { if (showBackButton) BackButton() },
        actions = {
            IconButton(onClick = { action() }) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

/** Same bar, but the action button shows custom content (for example a profile photo). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterTopAppBar(
    title: String,
    action: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    actionContent: @Composable () -> Unit
) {
    CenterAlignedTopAppBar(
        colors = appBarColors(),
        title = {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            IconButton(onClick = { action() }) {
                actionContent()
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CenterTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    showBackButton: Boolean = false
) {
    CenterAlignedTopAppBar(
        colors = appBarColors(),
        title = {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = { if (showBackButton) BackButton() },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun BackButton() {
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    IconButton(onClick = { dispatcher?.onBackPressed() }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun appBarColors(): TopAppBarColors {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        TopAppBarDefaults.centerAlignedTopAppBarColors()
    } else {
        TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = if (isSystemInDarkTheme()) DarkGrey else LightYellow
        )
    }
}