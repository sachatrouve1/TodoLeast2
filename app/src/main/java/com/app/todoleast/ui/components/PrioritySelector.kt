package com.app.todoleast.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.app.todoleast.model.Priority
import com.app.todoleast.ui.theme.PriorityHigh
import com.app.todoleast.ui.theme.PriorityLow
import com.app.todoleast.ui.theme.PriorityMedium

@Composable
fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PriorityOption.entries.forEach { option ->
            PriorityChip(
                label = option.label,
                color = option.color,
                selected = selectedPriority == option.priority,
                onClick = { onPrioritySelected(option.priority) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PriorityChip(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) color.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(200),
        label = "priorityBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) color else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(200),
        label = "priorityBorder"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private enum class PriorityOption(val priority: Priority, val label: String, val color: Color) {
    LOW(Priority.LOW, "Basse", PriorityLow),
    MEDIUM(Priority.MEDIUM, "Moyenne", PriorityMedium),
    HIGH(Priority.HIGH, "Haute", PriorityHigh)
}
