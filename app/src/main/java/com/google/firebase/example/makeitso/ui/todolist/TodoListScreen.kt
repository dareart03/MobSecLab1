package com.google.firebase.example.makeitso.ui.todolist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.example.makeitso.R
import com.google.firebase.example.makeitso.data.model.Priority
import com.google.firebase.example.makeitso.data.model.TodoItem
import com.google.firebase.example.makeitso.ui.shared.CenterTopAppBar
import com.google.firebase.example.makeitso.ui.shared.ProfileAvatar
import com.google.firebase.example.makeitso.ui.theme.DarkBlue
import com.google.firebase.example.makeitso.ui.theme.DarkGrey
import com.google.firebase.example.makeitso.ui.theme.MakeItSoTheme
import com.google.firebase.example.makeitso.ui.theme.MediumGrey
import com.google.firebase.example.makeitso.ui.theme.MediumYellow
import kotlinx.serialization.Serializable

//@Serializable
//data class TodoListRoute(val listId: String)
//TODO: Use listId when multiple lists feature is implemented

@Serializable
object TodoListRoute

@Composable
fun TodoListScreen(
    openSettingsScreen: () -> Unit,
    openTodoItemScreen: (String) -> Unit,
    viewModel: TodoListViewModel = hiltViewModel()
) {
    val todoItems = viewModel.todoItems.collectAsStateWithLifecycle(emptyList())

    TodoListScreenContent(
        todoItems = todoItems.value,
        photoUrl = viewModel.photoUrl,
        openSettingsScreen = openSettingsScreen,
        openTodoItemScreen = openTodoItemScreen,
        updateItem = viewModel::updateItem
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TodoListScreenContent(
    todoItems: List<TodoItem>,
    photoUrl: String?,
    openSettingsScreen: () -> Unit,
    openTodoItemScreen: (String) -> Unit,
    updateItem: (todoItem: TodoItem) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            CenterTopAppBar(
                title = stringResource(R.string.app_name),
                action = openSettingsScreen,
                scrollBehavior = scrollBehavior
            ) {
                ProfileAvatar(photoUrl = photoUrl, size = 32.dp)
            }
        },
    ) { innerPadding ->
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = 4.dp,
                    end = 4.dp,
                    bottom = 4.dp
                )
        ) {
            val (list, fab) = createRefs()
            val listState = rememberLazyListState()

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(list) {
                        top.linkTo(parent.top)
                        bottom.linkTo(fab.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        height = Dimension.fillToConstraints
                    },
                contentPadding = PaddingValues(8.dp)
            ) {
                items(todoItems) { todoItem ->
                    TodoItem(todoItem, openTodoItemScreen) {
                        updateItem(todoItem.copy(completed = it))
                    }
                }
            }

            ExtendedFloatingActionButton(
                modifier = Modifier
                    .constrainAs(fab) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
                    .padding(horizontal = 16.dp),
                containerColor = DarkBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, "Create list button") },
                text = { Text(text = stringResource(R.string.create_todo_item)) },
                onClick = { openTodoItemScreen("") }
            )
        }
    }
}

@Composable
fun TodoItem(
    todoItem: TodoItem,
    openTodoItemScreen: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val priority = Priority.entries.firstOrNull { it.value == todoItem.priority } ?: Priority.NONE
    val darkTheme = isSystemInDarkTheme()

    // Row colour = priority (red / orange / green); no priority keeps the app's neutral colour.
    val rowColor = when {
        priority != Priority.NONE -> priority.selectedColor
        darkTheme -> MediumGrey
        else -> MediumYellow
    }
    val contentColor = if (priority == Priority.NONE && darkTheme) Color.White else DarkGrey

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(rowColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = todoItem.completed,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = DarkBlue,
                uncheckedColor = contentColor,
                checkmarkColor = Color.White
            )
        )

        Text(
            text = todoItem.title,
            color = contentColor,
            modifier = Modifier.weight(1f).clickable(
                interactionSource = null,
                indication = null
            ) { openTodoItemScreen(todoItem.id) }
        )

        if (todoItem.flagged) {
            Icon(
                painter = painterResource(R.drawable.ic_flag),
                contentDescription = "Flag icon",
                tint = contentColor,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun TodoListScreenPreview() {
    MakeItSoTheme(darkTheme = true) {
        TodoListScreenContent(
            todoItems = listOf(TodoItem()),
            photoUrl = null,
            openSettingsScreen = {},
            openTodoItemScreen = {},
            updateItem = {}
        )
    }
}