package com.app.todoleast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.todoleast.data.RewardsPreferences
import com.app.todoleast.data.TaskRepository

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val rewardsPreferences: RewardsPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository, rewardsPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
