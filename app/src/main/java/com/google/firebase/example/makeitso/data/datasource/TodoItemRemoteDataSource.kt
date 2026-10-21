package com.google.firebase.example.makeitso.data.datasource

import com.google.firebase.example.makeitso.data.model.TodoItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TodoItemRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTodoItems(currentUserIdFlow: Flow<String?>): Flow<List<TodoItem>> {
        return currentUserIdFlow.flatMapLatest { ownerId ->
            if (ownerId == null) {
                // Signed out: nothing to show (and the server would reject an ownerId == null query).
                flowOf(emptyList<TodoItem>())
            } else {
                firestore
                    .collection(TODO_ITEMS_COLLECTION)
                    .whereEqualTo(OWNER_ID_FIELD, ownerId)
                    .dataObjects<TodoItem>()
                    // During sign-out the server may reject the old listener before it is
                    // cancelled; don't let that crash the UI.
                    .catch { emit(emptyList()) }
            }
        }
    }

    suspend fun getTodoItem(itemId: String): TodoItem? {
        return firestore.collection(TODO_ITEMS_COLLECTION).document(itemId).get().await().toObject()
    }

    suspend fun create(todoItem: TodoItem): String {
        return firestore.collection(TODO_ITEMS_COLLECTION).add(todoItem).await().id
    }

    suspend fun update(todoItem: TodoItem) {
        firestore.collection(TODO_ITEMS_COLLECTION).document(todoItem.id).set(todoItem).await()
    }

    suspend fun delete(itemId: String) {
        firestore.collection(TODO_ITEMS_COLLECTION).document(itemId).delete().await()
    }

    companion object {
        private const val OWNER_ID_FIELD = "ownerId"
        private const val TODO_ITEMS_COLLECTION = "todoitems"
    }
}