package com.meriniguan.todo.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.share(): LiveData<T> = this