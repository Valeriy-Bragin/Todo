package com.meriniguan.todo.di

import com.meriniguan.todo.model.task.TasksRepository
import com.meriniguan.todo.model.task.room.RoomTasksRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTaskRepository(
        tasksRepository: RoomTasksRepository
    ): TasksRepository
}