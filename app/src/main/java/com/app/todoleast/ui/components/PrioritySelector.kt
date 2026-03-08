package com.app.todoleast.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.todoleast.model.Priority

@Composable
fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PriorityOption.entries.forEach { option ->
            FilterChip(
                selected = selectedPriority == option.priority,
                onClick = { onPrioritySelected(option.priority) },
                label = { Text(option.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = option.color.copy(alpha = 0.2f),
                    selectedLabelColor = option.color
                )
            )
        }
    }
}

private enum class PriorityOption(val priority: Priority, val label: String, val color: Color) {
    LOW(Priority.LOW, "Basse", Color(0xFF4CAF50)),
    MEDIUM(Priority.MEDIUM, "Moyenne", Color(0xFFFF9800)),
    HIGH(Priority.HIGH, "Haute", Color(0xFFF44336))
}
