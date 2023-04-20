package com.meriniguan.todo.model.task

import androidx.paging.PagingData
import com.meriniguan.todo.model.preferences.SortOrder
import com.meriniguan.todo.model.task.entities.Task
import kotlinx.coroutines.flow.Flow

interface TasksRepository {

    fun getPagedTasks(searchQuery: String, sortOrder: SortOrder, hideCompleted: Boolean) : Flow<PagingData<Task>>

    suspend fun addTask(task: Task)

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(task: Task)

    suspend fun deleteAllCompletedTasks()
}