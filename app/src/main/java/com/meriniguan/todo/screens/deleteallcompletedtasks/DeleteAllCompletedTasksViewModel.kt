package com.meriniguan.todo.screens.deleteallcompletedtasks

import androidx.lifecycle.ViewModel
import com.meriniguan.todo.DELETE_ALL_COMPLETED_TASKS_RESULT_OK
import com.meriniguan.todo.di.ApplicationScope
import com.meriniguan.todo.model.task.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val DELETE_ALL_COMPLETED_TASKS_REQUEST_KEY = "delete_all_completed_tasks"
const val DELETE_ALL_COMPLETED_TASKS_RESULT_KEY = "delete_all_completed_tasks_result"

@HiltViewModel
class DeleteAllCompletedTasksViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    @ApplicationScope val applicationScope: CoroutineScope
) : ViewModel() {

    private val eventChannel = Channel<Event>()
    val eventFlow = eventChannel.receiveAsFlow()

    fun onConfirmClick() = applicationScope.launch {
        tasksRepository.deleteAllCompletedTasks()
        eventChannel.send(
            Event.SetFragmentResult(
                DELETE_ALL_COMPLETED_TASKS_REQUEST_KEY,
                DELETE_ALL_COMPLETED_TASKS_RESULT_KEY,
                DELETE_ALL_COMPLETED_TASKS_RESULT_OK
            )
        )
    }

    sealed class Event {

        data class SetFragmentResult(
            val requestKey: String,
            val resultKey: String,
            val result: Int
        ) : Event()
    }
}