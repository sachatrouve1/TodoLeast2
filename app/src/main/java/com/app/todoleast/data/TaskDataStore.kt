package com.app.todoleast.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.todoleast.model.Category
import com.app.todoleast.model.Priority
import com.app.todoleast.model.Repeat
import com.app.todoleast.model.Task
import com.app.todoleast.model.TaskStatus
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private val Context.taskDataStore: DataStore<Preferences> by preferencesDataStore(name = "tasks")

class TaskDataStore(private val context: Context) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    private val tasksKey = stringPreferencesKey("tasks_json")

    val tasks: Flow<List<Task>> = context.taskDataStore.data.map { preferences ->
        val json = preferences[tasksKey] ?: "[]"
        val type = object : TypeToken<List<TaskDto>>() {}.type
        val dtos: List<TaskDto> = gson.fromJson(json, type)
        dtos.map { it.toTask() }
    }

    suspend fun saveTasks(tasks: List<Task>) {
        val dtos = tasks.map { TaskDto.fromTask(it) }
        val json = gson.toJson(dtos)
        context.taskDataStore.edit { preferences ->
            preferences[tasksKey] = json
        }
    }
}

private data class TaskDto(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val createdAt: LocalDate,
    val completedAt: LocalDate?,
    val status: String,
    val repeat: String,
    val priority: String,
    val periodStartedAt: LocalDateTime?,
    val photoUri: String?,
    val category: String? = null
) {
    fun toTask(): Task = Task(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        dueTime = dueTime,
        createdAt = createdAt,
        completedAt = completedAt,
        status = TaskStatus.valueOf(status),
        repeat = Repeat.valueOf(repeat),
        priority = Priority.valueOf(priority),
        periodStartedAt = periodStartedAt,
        photoUri = photoUri,
        category = category?.let { Category.valueOf(it) } ?: Category.NONE
    )

    companion object {
        fun fromTask(task: Task): TaskDto = TaskDto(
            id = task.id,
            title = task.title,
            description = task.description,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            createdAt = task.createdAt,
            completedAt = task.completedAt,
            status = task.status.name,
            repeat = task.repeat.name,
            priority = task.priority.name,
            periodStartedAt = task.periodStartedAt,
            photoUri = task.photoUri,
            category = task.category.name
        )
    }
}

private class LocalDateAdapter : TypeAdapter<LocalDate?>() {
    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) out.nullValue()
        else out.value(value.toString())
    }
    override fun read(reader: JsonReader): LocalDate? {
        return if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            LocalDate.parse(reader.nextString())
        }
    }
}

private class LocalTimeAdapter : TypeAdapter<LocalTime?>() {
    override fun write(out: JsonWriter, value: LocalTime?) {
        if (value == null) out.nullValue()
        else out.value(value.toString())
    }
    override fun read(reader: JsonReader): LocalTime? {
        return if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            LocalTime.parse(reader.nextString())
        }
    }
}

private class LocalDateTimeAdapter : TypeAdapter<LocalDateTime?>() {
    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) out.nullValue()
        else out.value(value.toString())
    }
    override fun read(reader: JsonReader): LocalDateTime? {
        return if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            LocalDateTime.parse(reader.nextString())
        }
    }
}
