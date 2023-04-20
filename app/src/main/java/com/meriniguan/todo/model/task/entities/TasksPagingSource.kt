package com.meriniguan.todo.model.task.entities

import androidx.paging.PagingSource
import androidx.paging.PagingState

typealias TasksPageLoader = suspend (pageSize: Int, pageIndex: Int) -> List<Task>

class TasksPagingSource(
    private val loader: TasksPageLoader
) : PagingSource<Int, Task>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Task> {
        val pageIndex = params.key ?: 0

        return try {
            val tasks = loader(params.loadSize, pageIndex)
            LoadResult.Page(
                data = tasks,
                prevKey = if (pageIndex == 0) null else pageIndex - 1,
                nextKey = if (tasks.size == params.loadSize) pageIndex + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(throwable = e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Task>): Int? {
        // get the most recently accessed index in the tasks list:
        val anchorPosition = state.anchorPosition ?: return null
        // convert item index to page index:
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        // page doesn't have 'currentKey' property, so need to calculate it manually:
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }
}