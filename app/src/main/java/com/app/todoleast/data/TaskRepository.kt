package com.app.todoleast.data

import com.app.todoleast.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks().map { entities ->
        entities.map { it.toTask() }
    }

    suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)?.toTask()
    }

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(TaskEntity.fromTask(task))
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(TaskEntity.fromTask(task))
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun deleteCompletedTasks() {
        taskDao.deleteCompletedNonPeriodicTasks()
    }
}
