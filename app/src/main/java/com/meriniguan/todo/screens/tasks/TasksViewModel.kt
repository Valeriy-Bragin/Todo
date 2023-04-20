package com.meriniguan.todo.screens.tasks

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.meriniguan.todo.ADD_TASK_RESULT_OK
import com.meriniguan.todo.DELETE_ALL_COMPLETED_TASKS_RESULT_OK
import com.meriniguan.todo.EDIT_TASK_RESULT_OK
import com.meriniguan.todo.R
import com.meriniguan.todo.model.preferences.PreferencesManager
import com.meriniguan.todo.model.preferences.SortOrder
import com.meriniguan.todo.model.task.TasksRepository
import com.meriniguan.todo.model.task.entities.Task
import com.meriniguan.todo.screens.addedittask.ADD_EDIT_TASK_RESULT_KEY
import com.meriniguan.todo.screens.deleteallcompletedtasks.DELETE_ALL_COMPLETED_TASKS_RESULT_KEY
import com.meriniguan.todo.screens.tasks.adapters.TasksAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class TasksViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val preferencesManager: PreferencesManager,
    state: SavedStateHandle
) : ViewModel(), TasksAdapter.OnItemClickListener {

    private val searchQuery = state.getLiveData("search_query", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val localChanges = MutableStateFlow<Map<Long, Boolean>>(emptyMap())

    val tasks: Flow<PagingData<Task>>

    private var isHideCompletedSet = false

    init {
        val originalFlow = combine(
            searchQuery.asFlow(),
            preferencesFlow
        ) { searchQuery, filterPreferences ->
            isHideCompletedSet = filterPreferences.hideCompleted
            Triple(searchQuery, filterPreferences.sortOrder, filterPreferences.hideCompleted)
        }
            .debounce(500)
            .flatMapLatest { (searchQuery, sortOrder, hideCompleted) ->
                tasksRepository.getPagedTasks(searchQuery, sortOrder, hideCompleted)
            }
            .cachedIn(viewModelScope)

        tasks = combine(
            originalFlow,
            localChanges,
            ::merge
        )
    }

    private val eventChannel = Channel<Event>()
    val eventFlow = eventChannel.receiveAsFlow()

    fun onSearchQueryChanged(query: String) {
        if (searchQuery.value != query) {
            searchQuery.value = query
        }
    }

    fun getSearchQuery() = searchQuery.value

    fun onSortTasksByNameSelected() = viewModelScope.launch {
        preferencesManager.updateSortOrder(SortOrder.BY_NAME)
    }

    fun onSortTasksByDateCreatedSelected() = viewModelScope.launch {
        preferencesManager.updateSortOrder(SortOrder.BY_DATE_CREATED)
    }

    fun onHideCompletedTasksClick(isChecked: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(isChecked)
    }

    fun onDeleteAllCompletedTasksClick() = viewModelScope.launch{
        eventChannel.send(Event.ShowConfirmDeleteAllCompletedTasksScreen)
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        tasksRepository.deleteTask(task)
        eventChannel.send(
            Event.ShowUndoTaskDeletionMessage(R.string.task_deleted, R.string.undo, task)
        )
        eventChannel.send(Event.InvalidateTasksList)
    }

    fun onUndoTaskDeletionClick(task: Task) = viewModelScope.launch {
        tasksRepository.addTask(task)
        eventChannel.send(Event.InvalidateTasksList)
    }

    override fun onItemClick(task: Task) {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateToAddEditTaskScreen(R.string.edit_task, task))
        }
    }

    override fun onCompletedCheckBoxCheckedChange(task: Task, isChecked: Boolean) {
        viewModelScope.launch {
            tasksRepository.updateTask(task.copy(isCompleted = isChecked))
            val changes = localChanges.value.toMutableMap()
            changes[task.id] = isChecked
            localChanges.value = changes
            if (isHideCompletedSet) {
                eventChannel.send(Event.InvalidateTasksList)
            }
        }
    }

    fun onAddClick() = viewModelScope.launch {
        eventChannel.send(Event.NavigateToAddEditTaskScreen(R.string.add_task))
    }

    fun onAddEditTaskResult(bundle: Bundle) = viewModelScope.launch {
        when (bundle.getInt(ADD_EDIT_TASK_RESULT_KEY)) {
            ADD_TASK_RESULT_OK -> {
                handleResult(R.string.task_added)
            }
            EDIT_TASK_RESULT_OK -> {
                handleResult(R.string.task_edited)
            }
        }
    }

    fun onDeleteAllCompletedTasksResult(bundle: Bundle) = viewModelScope.launch {
        when (bundle.getInt(DELETE_ALL_COMPLETED_TASKS_RESULT_KEY)) {
            DELETE_ALL_COMPLETED_TASKS_RESULT_OK -> {
                eventChannel.send(Event.InvalidateTasksList)
            }
        }
    }

    private suspend fun handleResult(messageRes: Int) {
        eventChannel.send(Event.ShowMessage(messageRes))
        eventChannel.send(Event.InvalidateTasksList)
    }

    private fun merge(tasks: PagingData<Task>, localChanges: Map<Long, Boolean>): PagingData<Task> {
        return tasks.map {task ->
            if (localChanges.containsKey(task.id)) {
                task.copy(isCompleted = localChanges[task.id]!!)
            } else {
                task
            }
        }
    }

    sealed class Event {

        data class ShowUndoTaskDeletionMessage(
            @StringRes val messageRes: Int,
            @StringRes val undoButtonTextRes: Int,
            val task: Task
        ) : Event()

        object InvalidateTasksList : Event()

        object ShowConfirmDeleteAllCompletedTasksScreen : Event()

        data class NavigateToAddEditTaskScreen(
            @StringRes val titleRes: Int,
            val task: Task? = null
        ) : Event()

        data class ShowMessage(@StringRes val messageRes: Int) : Event()
    }
}