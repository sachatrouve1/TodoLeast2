package com.app.todoleast.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.app.todoleast.model.Repeat
import com.app.todoleast.model.Task
import com.app.todoleast.model.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime

data class CelebrationState(
    val task: Task? = null,
    val position: Offset = Offset.Zero
)

class TaskViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _selectedFilter = MutableStateFlow<TaskStatus?>(null)
    val selectedFilter: StateFlow<TaskStatus?> = _selectedFilter.asStateFlow()

    private val _celebrationState = MutableStateFlow(CelebrationState())
    val celebrationState: StateFlow<CelebrationState> = _celebrationState.asStateFlow()

    fun setFilter(status: TaskStatus?) {
        _selectedFilter.value = status
    }

    fun clearCelebration() {
        _celebrationState.value = CelebrationState()
    }

    fun getOverdueTasks(): List<Task> {
        return _tasks.value.filter { it.getEffectiveStatus() == TaskStatus.OVERDUE }
    }

    fun addTask(
        title: String,
        description: String = "",
        dueDate: LocalDate? = null,
        dueTime: LocalTime? = null,
        repeat: Repeat = Repeat.NONE
    ) {
        if (title.isBlank()) return

        val newTask = Task(
            title = title.trim(),
            description = description.trim(),
            dueDate = dueDate,
            dueTime = dueTime,
            status = TaskStatus.TO_DO,
            repeat = repeat
        )

        _tasks.update { currentTasks ->
            currentTasks + newTask
        }
    }

    fun deleteTask(taskId: String) {
        _tasks.update { currentTasks ->
            currentTasks.filter { it.id != taskId }
        }
    }

    fun deleteCompletedTasks() {
        _tasks.update { currentTasks ->
            currentTasks.filter { it.status != TaskStatus.COMPLETED }
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
        dueTime: LocalTime?,
        repeat: Repeat = Repeat.NONE
    ) {
        if (title.isBlank()) return

        _tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        title = title.trim(),
                        description = description.trim(),
                        dueDate = dueDate,
                        dueTime = dueTime,
                        repeat = repeat
                    )
                } else {
                    task
                }
            }
        }
    }

    fun toggleTaskCompletion(taskId: String, clickPosition: Offset = Offset.Zero) {
        val task = _tasks.value.find { it.id == taskId } ?: return
        val wasCompleted = task.status == TaskStatus.COMPLETED

        _tasks.update { currentTasks ->
            val updatedTasks = currentTasks.map { t ->
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

            // Si la tache a une periodicite, creer une nouvelle occurrence
            if (!wasCompleted && task.repeat != Repeat.NONE && task.dueDate != null) {
                val nextDueDate = when (task.repeat) {
                    Repeat.DAILY -> task.dueDate.plusDays(1)
                    Repeat.WEEKLY -> task.dueDate.plusWeeks(1)
                    Repeat.MONTHLY -> task.dueDate.plusMonths(1)
                    Repeat.NONE -> null
                }

                if (nextDueDate != null) {
                    val newTask = Task(
                        title = task.title,
                        description = task.description,
                        dueDate = nextDueDate,
                        dueTime = task.dueTime,
                        status = TaskStatus.TO_DO,
                        repeat = task.repeat
                    )
                    updatedTasks + newTask
                } else {
                    updatedTasks
                }
            } else {
                updatedTasks
            }
        }

        // Trigger celebration effect when completing a task
        if (!wasCompleted) {
            _celebrationState.value = CelebrationState(task = task, position = clickPosition)
        }
    }
}
