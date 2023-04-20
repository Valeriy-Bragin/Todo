package com.meriniguan.todo.model.task.room

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.meriniguan.todo.model.preferences.SortOrder
import com.meriniguan.todo.model.task.TasksRepository
import com.meriniguan.todo.model.task.entities.Task
import com.meriniguan.todo.model.task.entities.TasksPageLoader
import com.meriniguan.todo.model.task.entities.TasksPagingSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomTasksRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val ioDispatcher: CoroutineDispatcher
) : TasksRepository {

    override fun getPagedTasks(
        searchQuery: String,
        sortOrder: SortOrder,
        hideCompleted: Boolean
    ): Flow<PagingData<Task>> {
        val loader: TasksPageLoader = { pageSize, pageIndex ->
            getTasks(pageSize, pageIndex, searchQuery, sortOrder, hideCompleted)
        }
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize =  PAGE_SIZE,
                prefetchDistance = PAGE_SIZE / 2,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { TasksPagingSource(loader) }
        ).flow
    }

    override suspend fun addTask(task: Task) = withContext(ioDispatcher) {
        taskDao.addTask(TaskDbEntity.fromTask(task))
    }

    override suspend fun updateTask(task: Task) = withContext(ioDispatcher) {
        taskDao.updateTask(TaskDbEntity.fromTask(task))
    }

    override suspend fun deleteTask(task: Task) = withContext(ioDispatcher) {
        taskDao.deleteTask(TaskDbEntity.fromTask(task))
    }

    override suspend fun deleteAllCompletedTasks() = withContext(ioDispatcher) {
        taskDao.deleteAllCompletedTasks()
    }

    private suspend fun getTasks(
        pageSize: Int,
        pageIndex: Int,
        searchInput: String,
        sortOrder: SortOrder,
        hideCompleted: Boolean
    ): List<Task> = withContext(ioDispatcher) {
        val offset = pageSize * pageIndex
        return@withContext taskDao.getTasks(pageSize, offset, searchInput, sortOrder, hideCompleted)
            .map(TaskDbEntity::toTask)
    }

    companion object {
        const val PAGE_SIZE = 80
    }
}