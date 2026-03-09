package com.app.todoleast.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
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
    val priority: Priority = Priority.MEDIUM,
    val periodStartedAt: LocalDateTime? = null,
    val photoUri: String? = null
) {
    fun isOverdue(): Boolean {
        if (status == TaskStatus.COMPLETED) return false
        if (repeat != Repeat.NONE) return false
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

    fun isPeriodic(): Boolean = repeat != Repeat.NONE

    fun getRemainingTime(): Duration? {
        if (!isPeriodic()) return null
        val startTime = periodStartedAt ?: return null

        val periodEnd = when (repeat) {
            Repeat.DAILY -> startTime.plusDays(1)
            Repeat.WEEKLY -> startTime.plusWeeks(1)
            Repeat.MONTHLY -> startTime.plusMonths(1)
            Repeat.NONE -> return null
        }

        val now = LocalDateTime.now()
        return if (now.isBefore(periodEnd)) {
            Duration.between(now, periodEnd)
        } else {
            Duration.ZERO
        }
    }

    fun isPeriodExpired(): Boolean {
        val remaining = getRemainingTime() ?: return false
        return remaining.isZero || remaining.isNegative
    }
}
