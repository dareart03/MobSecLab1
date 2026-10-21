package com.google.firebase.example.makeitso.ui.todolist

import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.data.model.TodoItem
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import com.google.firebase.example.makeitso.data.repository.TodoItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Flagged items first, then by priority (high -> medium -> low -> none), then by title.
private val todoItemOrder = compareByDescending<TodoItem> { it.flagged }
    .thenByDescending { it.priority }
    .thenBy { it.title.lowercase() }

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val todoItemRepository: TodoItemRepository
) : MainViewModel() {
    val todoItems = todoItemRepository
        .getTodoItems(authRepository.currentUserIdFlow)
        .map { items -> items.sortedWith(todoItemOrder) }

    val photoUrl: String? = authRepository.currentPhotoUrl

    fun updateItem(item: TodoItem) {
        launchCatching {
            todoItemRepository.update(item)
        }
    }
}