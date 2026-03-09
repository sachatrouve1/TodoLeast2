package com.app.todoleast.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.todoleast.model.Priority
import com.app.todoleast.model.Repeat
import com.app.todoleast.model.Task
import com.app.todoleast.model.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val dueDateEpochDay: Long?,
    val dueTimeSecondOfDay: Int?,
    val createdAtEpochDay: Long,
    val completedAtEpochDay: Long?,
    val status: String,
    val repeat: String,
    val priority: String,
    val periodStartedAtEpochSecond: Long?,
    val photoUri: String?
) {
    fun toTask(): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            dueDate = dueDateEpochDay?.let { LocalDate.ofEpochDay(it) },
            dueTime = dueTimeSecondOfDay?.let { LocalTime.ofSecondOfDay(it.toLong()) },
            createdAt = LocalDate.ofEpochDay(createdAtEpochDay),
            completedAt = completedAtEpochDay?.let { LocalDate.ofEpochDay(it) },
            status = TaskStatus.valueOf(status),
            repeat = Repeat.valueOf(repeat),
            priority = Priority.valueOf(priority),
            periodStartedAt = periodStartedAtEpochSecond?.let {
                LocalDateTime.ofEpochSecond(it, 0, java.time.ZoneOffset.UTC)
            },
            photoUri = photoUri
        )
    }

    companion object {
        fun fromTask(task: Task): TaskEntity {
            return TaskEntity(
                id = task.id,
                title = task.title,
                description = task.description,
                dueDateEpochDay = task.dueDate?.toEpochDay(),
                dueTimeSecondOfDay = task.dueTime?.toSecondOfDay(),
                createdAtEpochDay = task.createdAt.toEpochDay(),
                completedAtEpochDay = task.completedAt?.toEpochDay(),
                status = task.status.name,
                repeat = task.repeat.name,
                priority = task.priority.name,
                periodStartedAtEpochSecond = task.periodStartedAt?.toEpochSecond(java.time.ZoneOffset.UTC),
                photoUri = task.photoUri
            )
        }
    }
}
