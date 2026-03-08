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

    private val _selectedFilter = MutableStateFlow<TaskStatus?>(null)
    val selectedFilter: StateFlow<TaskStatus?> = _selectedFilter.asStateFlow()

    fun setFilter(status: TaskStatus?) {
        _selectedFilter.value = status
    }

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

    fun getFilteredTasks(): List<Task> {
        val filter = _selectedFilter.value
        val filteredTasks = if (filter == null) {
            _tasks.value
        } else {
            _tasks.value.filter { it.getEffectiveStatus() == filter }
        }

        return filteredTasks.sortedWith(
            compareBy<Task> { it.status == TaskStatus.COMPLETED }
                .thenBy { it.dueDate ?: LocalDate.MAX }
                .thenBy { it.createdAt }
        )
    }

    fun getTaskById(taskId: String): Task? {
        return _tasks.value.find { it.id == taskId }
    }

    fun updateTask(
        taskId: String,
        title: String,
        description: String,
        dueDate: LocalDate?,
        dueTime: LocalTime?
    ) {
        if (title.isBlank()) return

        _tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        title = title.trim(),
                        description = description.trim(),
                        dueDate = dueDate,
                        dueTime = dueTime
                    )
                } else {
                    task
                }
            }
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        _tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.id == taskId) {
                    if (task.status == TaskStatus.COMPLETED) {
                        task.copy(
                            status = TaskStatus.TO_DO,
                            completedAt = null
                        )
                    } else {
                        task.copy(
                            status = TaskStatus.COMPLETED,
                            completedAt = LocalDate.now()
                        )
                    }
                } else {
                    task
                }
            }
        }
    }
}
