package com.meriniguan.todo.model.task.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.DateFormat

@Parcelize
data class Task(
    val name: String,
    val isCompleted: Boolean = false,
    val isImportant: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis(),
    val id: Long = 0
) : Parcelable {
    val dateCreatedFormatted: String
        get() = DateFormat.getDateTimeInstance().format(dateCreated)
}