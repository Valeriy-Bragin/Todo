package com.meriniguan.todo.model.task.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.meriniguan.todo.model.task.entities.Task

@Entity(tableName = "tasks", indices = [Index("name")])
data class TaskDbEntity(
    val name: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "is_important") val isImportant: Boolean = false,
    @ColumnInfo(name = "date_created") val dateCreated: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Long = 0
) {

    fun toTask(): Task = Task(
        name = name,
        isCompleted = isCompleted,
        isImportant = isImportant,
        dateCreated = dateCreated,
        id = id
    )

    companion object {
        fun fromTask(task: Task): TaskDbEntity = TaskDbEntity(
            name = task.name,
            isCompleted = task.isCompleted,
            isImportant = task.isImportant,
            dateCreated = task.dateCreated,
            id = task.id
        )
    }
}