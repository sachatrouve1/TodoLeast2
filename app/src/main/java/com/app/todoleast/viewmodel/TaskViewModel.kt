package com.app.todoleast.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.todoleast.data.TaskRepository
import com.app.todoleast.model.Priority
import com.app.todoleast.model.Repeat
import com.app.todoleast.model.RewardsState
import com.app.todoleast.model.Task
import com.app.todoleast.model.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class CelebrationState(
    val task: Task? = null,
    val position: Offset = Offset.Zero
)

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFilter = MutableStateFlow<TaskStatus?>(null)
    val selectedFilter: StateFlow<TaskStatus?> = _selectedFilter.asStateFlow()

    private val _celebrationState = MutableStateFlow(CelebrationState())
    val celebrationState: StateFlow<CelebrationState> = _celebrationState.asStateFlow()

    private val _rewardsState = MutableStateFlow(RewardsState())
    val rewardsState: StateFlow<RewardsState> = _rewardsState.asStateFlow()

    fun setFilter(status: TaskStatus?) {
        _selectedFilter.value = status
    }

    fun clearCelebration() {
        _celebrationState.value = CelebrationState()
    }

    fun clearNewAchievement() {
        _rewardsState.update { it.copy(newlyUnlockedAchievement = null) }
    }

    private fun awardPoints(task: Task) {
        if (task.id in _rewardsState.value.rewardedTaskIds) return

        val points = _rewardsState.value.getPointsForPriority(task.priority)
        val newCount = _rewardsState.value.completedTasksCount + 1
        val newAchievement = _rewardsState.value.checkNewAchievements(newCount)

        _rewardsState.update { current ->
            current.copy(
                totalPoints = current.totalPoints + points,
                completedTasksCount = newCount,
                unlockedAchievements = if (newAchievement != null) {
                    current.unlockedAchievements + newAchievement
                } else {
                    current.unlockedAchievements
                },
                newlyUnlockedAchievement = newAchievement,
                rewardedTaskIds = current.rewardedTaskIds + task.id
            )
        }
    }

    fun getOverdueTasks(): List<Task> {
        return tasks.value.filter { it.getEffectiveStatus() == TaskStatus.OVERDUE }
    }

    fun addTask(
        title: String,
        description: String = "",
        dueDate: LocalDate? = null,
        dueTime: LocalTime? = null,
        repeat: Repeat = Repeat.NONE,
        priority: Priority = Priority.MEDIUM,
        photoUri: String? = null
    ) {
        if (title.isBlank()) return

        val isPeriodic = repeat != Repeat.NONE
        val newTask = Task(
            title = title.trim(),
            description = description.trim(),
            dueDate = if (isPeriodic) null else dueDate,
            dueTime = if (isPeriodic) null else dueTime,
            status = TaskStatus.TO_DO,
            repeat = repeat,
            priority = priority,
            periodStartedAt = if (isPeriodic) LocalDateTime.now() else null,
            photoUri = photoUri
        )

        viewModelScope.launch {
            repository.insertTask(newTask)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            repository.deleteCompletedTasks()
        }
    }

    fun getFilteredTasks(): List<Task> {
        checkAndResetPeriodicTasks()

        val filter = _selectedFilter.value
        val nonPeriodicTasks = tasks.value.filter { !it.isPeriodic() }
        val filteredTasks = if (filter == null) {
            nonPeriodicTasks
        } else {
            nonPeriodicTasks.filter { it.getEffectiveStatus() == filter }
        }

        return filteredTasks.sortedWith(
            compareBy<Task> { it.status == TaskStatus.COMPLETED }
                .thenByDescending { it.priority.ordinal }
                .thenBy { it.dueDate ?: LocalDate.MAX }
                .thenBy { it.createdAt }
        )
    }

    fun getFilteredPeriodicTasks(repeat: Repeat, filter: TaskStatus? = null): List<Task> {
        return tasks.value
            .filter { it.repeat == repeat }
            .filter { filter == null || it.getEffectiveStatus() == filter }
            .sortedWith(
                compareBy<Task> { it.status == TaskStatus.COMPLETED }
                    .thenByDescending { it.priority.ordinal }
            )
    }

    fun getTaskById(taskId: String): Task? {
        return tasks.value.find { it.id == taskId }
    }

    fun updateTask(
        taskId: String,
        title: String,
        description: String,
        dueDate: LocalDate?,
        dueTime: LocalTime?,
        repeat: Repeat = Repeat.NONE,
        priority: Priority = Priority.MEDIUM,
        photoUri: String? = null
    ) {
        if (title.isBlank()) return

        val existingTask = tasks.value.find { it.id == taskId } ?: return
        val isPeriodic = repeat != Repeat.NONE
        val wasPeriodic = existingTask.repeat != Repeat.NONE

        val updatedTask = existingTask.copy(
            title = title.trim(),
            description = description.trim(),
            dueDate = if (isPeriodic) null else dueDate,
            dueTime = if (isPeriodic) null else dueTime,
            repeat = repeat,
            priority = priority,
            periodStartedAt = if (isPeriodic && !wasPeriodic) {
                LocalDateTime.now()
            } else if (isPeriodic) {
                existingTask.periodStartedAt
            } else {
                null
            },
            photoUri = photoUri
        )

        viewModelScope.launch {
            repository.updateTask(updatedTask)
        }
    }

    fun toggleTaskCompletion(taskId: String, clickPosition: Offset = Offset.Zero) {
        val task = tasks.value.find { it.id == taskId } ?: return
        val wasCompleted = task.status == TaskStatus.COMPLETED

        val updatedTask = if (wasCompleted) {
            task.copy(status = TaskStatus.TO_DO, completedAt = null)
        } else {
            task.copy(status = TaskStatus.COMPLETED, completedAt = LocalDate.now())
        }

        viewModelScope.launch {
            repository.updateTask(updatedTask)
        }

        if (!wasCompleted) {
            _celebrationState.value = CelebrationState(task = task, position = clickPosition)
            awardPoints(task)
        }
    }

    fun checkAndResetPeriodicTasks() {
        val tasksToReset = tasks.value
            .filter { it.isPeriodic() && it.isPeriodExpired() && it.status == TaskStatus.COMPLETED }

        if (tasksToReset.isEmpty()) return

        viewModelScope.launch {
            tasksToReset.forEach { task ->
                val resetTask = task.copy(
                    status = TaskStatus.TO_DO,
                    completedAt = null,
                    periodStartedAt = LocalDateTime.now()
                )
                repository.updateTask(resetTask)
            }
        }

        _rewardsState.update { current ->
            current.copy(
                rewardedTaskIds = current.rewardedTaskIds - tasksToReset.map { it.id }.toSet()
            )
        }
    }

    fun getPeriodicTasks(): List<Task> {
        return tasks.value.filter { it.isPeriodic() }
    }

    fun getNonPeriodicTasks(): List<Task> {
        return tasks.value.filter { !it.isPeriodic() }
    }
}
