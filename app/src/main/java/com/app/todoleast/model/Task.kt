package com.app.todoleast.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val createdAt: LocalDate = LocalDate.now(),
    val completedAt: LocalDate? = null,
    val status: TaskStatus = TaskStatus.TO_DO,
    val repeat: Repeat = Repeat.NONE,
    val priority: Priority = Priority.MEDIUM
) {
    fun isOverdue(): Boolean {
        if (status == TaskStatus.COMPLETED) return false
        val due = dueDate ?: return false
        return LocalDate.now().isAfter(due)
    }

    fun getEffectiveStatus(): TaskStatus {
        return when {
            status == TaskStatus.COMPLETED -> TaskStatus.COMPLETED
            isOverdue() -> TaskStatus.OVERDUE
            else -> status
        }
    }
}
