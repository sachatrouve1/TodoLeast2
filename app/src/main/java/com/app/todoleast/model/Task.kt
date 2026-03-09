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
    val photoUri: String? = null,
    val category: Category = Category.NONE
) {
    fun isOverdue(): Boolean {
        if (status == TaskStatus.COMPLETED) return false
        if (repeat != Repeat.NONE) return false
        val due = dueDate ?: return false

        val now = LocalDateTime.now()
        val today = now.toLocalDate()

        // If the due date is in the past, it's overdue
        if (today.isAfter(due)) return true

        // If the due date is today and we have a time, check if the time has passed
        if (today.isEqual(due) && dueTime != null) {
            return now.toLocalTime().isAfter(dueTime)
        }

        return false
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

        val now = LocalDateTime.now()
        val nextReset = getNextResetTime() ?: return null

        return if (now.isBefore(nextReset)) {
            Duration.between(now, nextReset)
        } else {
            Duration.ZERO
        }
    }

    fun getNextResetTime(): LocalDateTime? {
        if (!isPeriodic()) return null

        val now = LocalDateTime.now()

        return when (repeat) {
            Repeat.DAILY -> {
                // Tomorrow at midnight
                LocalDate.now().plusDays(1).atStartOfDay()
            }
            Repeat.WEEKLY -> {
                // Next Monday at midnight
                val dayOfWeek = now.dayOfWeek.value // Monday = 1, Sunday = 7
                val daysUntilMonday = if (dayOfWeek == 1) 7 else (8 - dayOfWeek)
                now.toLocalDate().plusDays(daysUntilMonday.toLong()).atStartOfDay()
            }
            Repeat.MONTHLY -> {
                // 1st of next month at midnight
                now.toLocalDate().plusMonths(1).withDayOfMonth(1).atStartOfDay()
            }
            Repeat.NONE -> null
        }
    }

    fun isPeriodExpired(): Boolean {
        val remaining = getRemainingTime() ?: return false
        return remaining.isZero || remaining.isNegative
    }
}
