package com.app.todoleast.data

import com.app.todoleast.model.Task
import com.app.todoleast.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TaskRepository(private val taskDataStore: TaskDataStore) {

    val allTasks: Flow<List<Task>> = taskDataStore.tasks

    suspend fun insertTask(task: Task) {
        val current = taskDataStore.tasks.first()
        taskDataStore.saveTasks(current + task)
    }

    suspend fun updateTask(task: Task) {
        val current = taskDataStore.tasks.first()
        val updated = current.map { if (it.id == task.id) task else it }
        taskDataStore.saveTasks(updated)
    }

    suspend fun deleteTask(taskId: String) {
        val current = taskDataStore.tasks.first()
        taskDataStore.saveTasks(current.filter { it.id != taskId })
    }

    suspend fun deleteCompletedTasks() {
        val current = taskDataStore.tasks.first()
        taskDataStore.saveTasks(current.filter {
            it.status != TaskStatus.COMPLETED || it.isPeriodic()
        })
    }
}
