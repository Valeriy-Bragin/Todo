package com.meriniguan.todo.screens.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meriniguan.todo.ADD_TASK_RESULT_OK
import com.meriniguan.todo.EDIT_TASK_RESULT_OK
import com.meriniguan.todo.model.task.TasksRepository
import com.meriniguan.todo.model.task.entities.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ADD_EDIT_TASK_REQUEST_KEY = "add_edit_task"
const val ADD_EDIT_TASK_RESULT_KEY = "add_edit_task_result"

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    val state: SavedStateHandle
) : ViewModel() {

    val task = state.get<Task>("task")

    var name = state.get<String>("name") ?: task?.name ?: ""
    set(value) {
        field = value
        state["name"] = value
    }

    var isImportant = state.get<Boolean>("isImportant") ?: task?.isImportant ?: false
        set(value) {
            field = value
            state["isImportant"] = value
        }

    val dateCreatedFormatted = task?.dateCreatedFormatted ?: ""

    private val eventChannel = Channel<Event>()
    val eventFlow = eventChannel.receiveAsFlow()

    fun onTaskNameEditTextTextChanged(newText: String) {
        name = newText
    }

    fun onImportantCheckboxCheckedChange(isChecked: Boolean) {
        isImportant = isChecked
    }

    fun onSaveClick() {
        if (isAdding()) {
            addTask()
        } else {
            editTask()
        }
    }

    private fun addTask() = viewModelScope.launch {
        tasksRepository.addTask(Task(name = name, isImportant = isImportant))
        eventChannel.send(
            Event.SetFragmentResult(
                ADD_EDIT_TASK_REQUEST_KEY,
                ADD_EDIT_TASK_RESULT_KEY,
                ADD_TASK_RESULT_OK
            )
        )
        eventChannel.send(Event.NavigateBack)
    }

    private fun editTask() = viewModelScope.launch {
        tasksRepository.updateTask(task!!.copy(name = name, isImportant = isImportant))
        eventChannel.send(
            Event.SetFragmentResult(
                ADD_EDIT_TASK_REQUEST_KEY,
                ADD_EDIT_TASK_RESULT_KEY,
                EDIT_TASK_RESULT_OK
            )
        )
        eventChannel.send(Event.NavigateBack)
    }

    private fun isAdding(): Boolean = task == null

    sealed class Event {

        data class SetFragmentResult(
            val requestKey: String,
            val resultKey: String,
            val result: Int
            ) : Event()

        object NavigateBack : Event()
    }
}