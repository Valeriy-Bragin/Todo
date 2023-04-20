package com.meriniguan.todo.di

import android.app.Application
import androidx.room.Room
import com.meriniguan.todo.model.AppDatabase
import com.meriniguan.todo.model.task.room.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        application: Application,
        callback: AppDatabase.Callback
    ): AppDatabase =
        Room.databaseBuilder(application, AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Provides
    @Singleton
    fun provideTaskDao(
        appDatabase: AppDatabase
    ): TaskDao = appDatabase.getTaskDao()
}