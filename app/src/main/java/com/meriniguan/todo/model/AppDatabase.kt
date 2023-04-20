package com.meriniguan.todo.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.meriniguan.todo.di.ApplicationScope
import com.meriniguan.todo.model.task.room.TaskDao
import com.meriniguan.todo.model.task.room.TaskDbEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [TaskDbEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getTaskDao(): TaskDao

    class Callback @Inject constructor(
        private val database: Provider<AppDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ): RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().getTaskDao()

            applicationScope.launch {
                for (i in 1..30) {
                    dao.addTask(TaskDbEntity("Wash the dishes $i"))
                    dao.addTask(TaskDbEntity("Do the laundry $i"))
                    dao.addTask(TaskDbEntity("Buy groceries $i", isImportant = true))
                    dao.addTask(TaskDbEntity("Prepare food $i", isCompleted = true))
                    dao.addTask(TaskDbEntity("Call mom $i"))
                    dao.addTask(TaskDbEntity("Visit grandma $i", isCompleted = true))
                    dao.addTask(TaskDbEntity("Repair my bike $i"))
                    dao.addTask(TaskDbEntity("Call Elon Musk $i"))
                }
            }
        }
    }
}