package com.app.todoleast.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.app.todoleast.model.Achievement
import com.app.todoleast.model.Priority
import com.app.todoleast.model.Repeat
import com.app.todoleast.model.RewardsState
import com.app.todoleast.model.Task
import com.app.todoleast.model.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime
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
        // Ne pas recompenser si la tache a deja ete recompensee
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
        return _tasks.value.filter { it.getEffectiveStatus() == TaskStatus.OVERDUE }
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
        // Check and reset expired periodic tasks
        checkAndResetPeriodicTasks()

        val filter = _selectedFilter.value
        // Only return non-periodic tasks in the main filtered list
        val nonPeriodicTasks = _tasks.value.filter { !it.isPeriodic() }
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
        return _tasks.value
            .filter { it.repeat == repeat }
            .filter { filter == null || it.getEffectiveStatus() == filter }
            .sortedWith(
                compareBy<Task> { it.status == TaskStatus.COMPLETED }
                    .thenByDescending { it.priority.ordinal }
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
        repeat: Repeat = Repeat.NONE,
        priority: Priority = Priority.MEDIUM,
        photoUri: String? = null
    ) {
        if (title.isBlank()) return

        _tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.id == taskId) {
                    val isPeriodic = repeat != Repeat.NONE
                    val wasPeriodic = task.repeat != Repeat.NONE
                    task.copy(
                        title = title.trim(),
                        description = description.trim(),
                        dueDate = if (isPeriodic) null else dueDate,
                        dueTime = if (isPeriodic) null else dueTime,
                        repeat = repeat,
                        priority = priority,
                        periodStartedAt = if (isPeriodic && !wasPeriodic) {
                            LocalDateTime.now()
                        } else if (isPeriodic) {
                            task.periodStartedAt
                        } else {
                            null
                        },
                        photoUri = photoUri
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

        // Trigger celebration effect and award points when completing a task
        if (!wasCompleted) {
            _celebrationState.value = CelebrationState(task = task, position = clickPosition)
            awardPoints(task)
        }
    }

    fun checkAndResetPeriodicTasks() {
        // Find tasks that need to be reset (period expired and completed)
        val tasksToReset = _tasks.value
            .filter { it.isPeriodic() && it.isPeriodExpired() && it.status == TaskStatus.COMPLETED }
            .map { it.id }
            .toSet()

        if (tasksToReset.isEmpty()) return

        _tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.id in tasksToReset) {
                    // Reset the task for the new period
                    task.copy(
                        status = TaskStatus.TO_DO,
                        completedAt = null,
                        periodStartedAt = LocalDateTime.now()
                    )
                } else {
                    task
                }
            }
        }

        // Only remove IDs of tasks that actually expired from rewarded list
        _rewardsState.update { current ->
            current.copy(
                rewardedTaskIds = current.rewardedTaskIds - tasksToReset
            )
        }
    }

    fun getPeriodicTasks(): List<Task> {
        return _tasks.value.filter { it.isPeriodic() }
    }

    fun getNonPeriodicTasks(): List<Task> {
        return _tasks.value.filter { !it.isPeriodic() }
    }
}
