package com.app.todoleast.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.todoleast.model.TaskStatus
import com.app.todoleast.ui.theme.StatusCompleted
import com.app.todoleast.ui.theme.StatusOverdue
import com.app.todoleast.ui.theme.StatusToDo

@Composable
fun FilterChips(
    selectedFilter: TaskStatus?,
    onFilterSelected: (TaskStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StyledFilterChip(
            label = "Toutes",
            selected = selectedFilter == null,
            color = MaterialTheme.colorScheme.primary,
            onClick = { onFilterSelected(null) }
        )

        StyledFilterChip(
            label = "A faire",
            selected = selectedFilter == TaskStatus.TO_DO,
            color = StatusToDo,
            onClick = { onFilterSelected(TaskStatus.TO_DO) }
        )

        StyledFilterChip(
            label = "En retard",
            selected = selectedFilter == TaskStatus.OVERDUE,
            color = StatusOverdue,
            onClick = { onFilterSelected(TaskStatus.OVERDUE) }
        )

        StyledFilterChip(
            label = "Terminées",
            selected = selectedFilter == TaskStatus.COMPLETED,
            color = StatusCompleted,
            onClick = { onFilterSelected(TaskStatus.COMPLETED) }
        )
    }
}

@Composable
private fun StyledFilterChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) color.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(200),
        label = "chipBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) color else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(200),
        label = "chipBorder"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "chipText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor
        )
    }
}
