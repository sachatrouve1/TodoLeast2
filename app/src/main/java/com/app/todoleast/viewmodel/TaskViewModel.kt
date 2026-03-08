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

    private val _justCompletedTask = MutableStateFlow<Task?>(null)
    val justCompletedTask: StateFlow<Task?> = _justCompletedTask.asStateFlow()

    fun setFilter(status: TaskStatus?) {
        _selectedFilter.value = status
    }

    fun clearJustCompletedTask() {
        _justCompletedTask.value = null
    }

    fun getOverdueTasks(): List<Task> {
        return _tasks.value.filter { it.getEffectiveStatus() == TaskStatus.OVERDUE }
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
        val task = _tasks.value.find { it.id == taskId } ?: return
        val wasCompleted = task.status == TaskStatus.COMPLETED

        _tasks.update { currentTasks ->
            currentTasks.map { t ->
                if (t.id == taskId) {
                    if (wasCompleted) {
                        t.copy(
                            status = TaskStatus.TO_DO,
                            completedAt = null
                        )
                    } else {
                        t.copy(
                            status = TaskStatus.COMPLETED,
                            completedAt = LocalDate.now()
                        )
                    }
                } else {
                    t
                }
            }
        }

        // Trigger celebration effect when completing a task
        if (!wasCompleted) {
            _justCompletedTask.value = task
        }
    }
}
