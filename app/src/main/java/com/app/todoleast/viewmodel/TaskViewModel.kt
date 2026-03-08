package com.app.todoleast.viewmodel

import androidx.lifecycle.ViewModel
import com.app.todoleast.model.Task
import com.app.todoleast.model.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime

class TaskViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    fun addTask(
        title: String,
        description: String = "",
        dueDate: LocalDate? = null,
        dueTime: LocalTime? = null
    ) {
        if (title.isBlank()) return

        val newTask = Task(
            title = title.trim(),
            description = description.trim(),
            dueDate = dueDate,
            dueTime = dueTime,
            status = TaskStatus.TO_DO
        )

        _tasks.update { currentTasks ->
            currentTasks + newTask
        }
    }

    fun getTasksSortedByDate(): List<Task> {
        return _tasks.value.sortedWith(
            compareBy<Task> { it.status == TaskStatus.COMPLETED }
                .thenBy { it.dueDate ?: LocalDate.MAX }
                .thenBy { it.createdAt }
        )
    }
}
