package com.meriniguan.todo.model.task.room

import androidx.room.*
import com.meriniguan.todo.model.preferences.SortOrder

@Dao
interface TaskDao {

    fun getTasks(limit: Int, offset: Int, searchQuery: String, sortOrder: SortOrder, hideCompleted: Boolean): List<TaskDbEntity> =
        when (sortOrder) {
            SortOrder.BY_DATE_CREATED -> getTasksSortedByDateCreated(limit, offset, searchQuery, hideCompleted)
            SortOrder.BY_NAME -> getTasksSortedByName(limit, offset, searchQuery, hideCompleted)
        }

    @Query("SELECT * FROM tasks " +
            "WHERE name LIKE '%' || :searchQuery || '%' " +
            "AND (is_completed != :hideCompleted OR is_completed = 0) " +
            "ORDER BY is_important DESC, date_created " +
            "LIMIT :limit OFFSET :offset")
    fun getTasksSortedByDateCreated(limit: Int, offset: Int, searchQuery: String, hideCompleted: Boolean): List<TaskDbEntity>

    @Query("SELECT * FROM tasks " +
            "WHERE name LIKE '%' || :searchQuery || '%' " +
            "AND (is_completed != :hideCompleted OR is_completed = 0) " +
            "ORDER BY is_important DESC, name " +
            "LIMIT :limit OFFSET :offset")
    fun getTasksSortedByName(limit: Int, offset: Int, searchQuery: String, hideCompleted: Boolean): List<TaskDbEntity>

    @Insert
    suspend fun addTask(taskDbEntity: TaskDbEntity)

    @Update
    suspend fun updateTask(taskDbEntity: TaskDbEntity)

    @Delete
    suspend fun deleteTask(taskDbEntity: TaskDbEntity)

    @Query("DELETE FROM tasks WHERE is_completed = 1")
    suspend fun deleteAllCompletedTasks()
}