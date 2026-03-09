package com.app.todoleast.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.todoleast.model.Category
import com.app.todoleast.model.Priority
import com.app.todoleast.model.Task
import com.app.todoleast.model.TaskStatus
import com.app.todoleast.ui.theme.PriorityHigh
import com.app.todoleast.ui.theme.PriorityLow
import com.app.todoleast.ui.theme.PriorityMedium
import com.app.todoleast.ui.theme.StatusCompleted
import com.app.todoleast.ui.theme.StatusOverdue
import com.app.todoleast.ui.theme.StatusToDo
import java.time.format.DateTimeFormatter

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onToggleCompletion: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveStatus = task.getEffectiveStatus()
    val isCompleted = effectiveStatus == TaskStatus.COMPLETED

    val statusColor = when (effectiveStatus) {
        TaskStatus.TO_DO -> StatusToDo
        TaskStatus.COMPLETED -> StatusCompleted
        TaskStatus.OVERDUE -> StatusOverdue
    }

    val priorityColor = when (task.priority) {
        Priority.LOW -> PriorityLow
        Priority.MEDIUM -> PriorityMedium
        Priority.HIGH -> PriorityHigh
    }

    var checkboxCenter by remember { mutableStateOf(Offset.Zero) }

    val cardAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.7f else 1f,
        animationSpec = tween(300),
        label = "cardAlpha"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = tween(200),
        label = "checkScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isCompleted) 1.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = priorityColor.copy(alpha = 0.15f)
            )
            .clickable { onTaskClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Custom checkbox with animation
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .onGloballyPositioned { coordinates ->
                        val bounds = coordinates.boundsInRoot()
                        checkboxCenter = Offset(
                            bounds.left + bounds.width / 2,
                            bounds.top + bounds.height / 2
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onToggleCompletion(checkboxCenter)
                        }
                    }
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) {
                            Brush.linearGradient(
                                colors = listOf(statusColor, statusColor.copy(alpha = 0.8f))
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(Color.Transparent, Color.Transparent)
                            )
                        }
                    )
                    .border(
                        width = 2.dp,
                        color = if (isCompleted) Color.Transparent else statusColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Terminee",
                        tint = Color.White,
                        modifier = Modifier
                            .size(16.dp)
                            .scale(checkScale)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (isCompleted) 0.4f else 0.8f
                        ),
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Date/time info or countdown
                if (task.isPeriodic()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    CountdownTimer(
                        remainingDuration = task.getRemainingTime(),
                        isCompleted = isCompleted
                    )
                } else if (task.dueDate != null || task.dueTime != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        task.dueDate?.let { date ->
                            DateChip(
                                icon = Icons.Outlined.CalendarToday,
                                text = date.format(DateTimeFormatter.ofPattern("dd MMM")),
                                isOverdue = effectiveStatus == TaskStatus.OVERDUE
                            )
                        }

                        task.dueTime?.let { time ->
                            DateChip(
                                icon = Icons.Outlined.Schedule,
                                text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                isOverdue = false
                            )
                        }
                    }
                }

                // Badges row
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriorityIndicator(priority = task.priority)
                    if (task.category != Category.NONE) {
                        CategoryChip(category = task.category)
                    }
                }
            }

            // Photo thumbnail
            task.photoUri?.let { uri ->
                Spacer(modifier = Modifier.width(12.dp))
                AsyncImage(
                    model = uri,
                    contentDescription = "Photo de la tache",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun DateChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isOverdue: Boolean
) {
    val color = if (isOverdue) StatusOverdue else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isOverdue)
            StatusOverdue.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = color
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PriorityIndicator(priority: Priority) {
    val (label, color) = when (priority) {
        Priority.LOW -> "Basse" to PriorityLow
        Priority.MEDIUM -> "Moyenne" to PriorityMedium
        Priority.HIGH -> "Haute" to PriorityHigh
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Priority dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CategoryChip(category: Category) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (category.emoji.isNotEmpty()) {
                Text(
                    text = category.emoji,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
