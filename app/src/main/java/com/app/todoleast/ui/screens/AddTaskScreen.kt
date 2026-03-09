package com.app.todoleast.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.app.todoleast.model.Priority
import com.app.todoleast.model.Repeat
import com.app.todoleast.ui.components.PhotoPicker
import com.app.todoleast.ui.components.PrioritySelector
import com.app.todoleast.ui.components.RepeatSelector
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onNavigateBack: () -> Unit,
    onTaskCreated: (title: String, description: String, dueDate: LocalDate?, dueTime: LocalTime?, repeat: Repeat, priority: Priority, photoUri: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<LocalDate?>(null) }
    var dueTime by remember { mutableStateOf<LocalTime?>(null) }
    var repeat by remember { mutableStateOf(Repeat.NONE) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val isFormValid = title.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nouvelle tache",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre *") },
                placeholder = { Text("Entrez le titre de la tache") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Ajoutez des details (optionnel)") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Repetition",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            RepeatSelector(
                selectedRepeat = repeat,
                onRepeatSelected = {
                    repeat = it
                    // Clear date/time when selecting periodic
                    if (it != Repeat.NONE) {
                        dueDate = null
                        dueTime = null
                    }
                }
            )

            // Only show date/time pickers for non-periodic tasks
            if (repeat == Repeat.NONE) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Echeance (optionnel)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date picker button
                OutlinedTextField(
                    value = dueDate?.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) ?: "",
                    onValueChange = {},
                    label = { Text("Date limite") },
                    placeholder = { Text("Selectionner une date") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (dueDate != null) {
                            TextButton(onClick = { dueDate = null }) {
                                Text("Effacer")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        .also { interactionSource ->
                            androidx.compose.runtime.LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                        showDatePicker = true
                                    }
                                }
                            }
                        }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time picker button
                OutlinedTextField(
                    value = dueTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "",
                    onValueChange = {},
                    label = { Text("Heure limite") },
                    placeholder = { Text("Selectionner une heure") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (dueTime != null) {
                            TextButton(onClick = { dueTime = null }) {
                                Text("Effacer")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        .also { interactionSource ->
                            androidx.compose.runtime.LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is androidx.compose.foundation.interaction.PressInteraction.Release) {
                                        showTimePicker = true
                                    }
                                }
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Priorite",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrioritySelector(
                selectedPriority = priority,
                onPrioritySelected = { priority = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            PhotoPicker(
                photoUri = photoUri,
                onPhotoSelected = { photoUri = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Create button
            Button(
                onClick = {
                    onTaskCreated(title, description, dueDate, dueTime, repeat, priority, photoUri)
                    onNavigateBack()
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Creer la tache",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            dueDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                dueTime = LocalTime.of(hour, minute)
                showTimePicker = false
            },
            initialHour = dueTime?.hour ?: 12,
            initialMinute = dueTime?.minute ?: 0
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
