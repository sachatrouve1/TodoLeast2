package com.app.todoleast.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.todoleast.model.TaskStatus
import com.app.todoleast.ui.theme.StatusCompleted
import com.app.todoleast.ui.theme.StatusOnGoing
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == null,
            onClick = { onFilterSelected(null) },
            label = { Text("Toutes") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        FilterChip(
            selected = selectedFilter == TaskStatus.TO_DO,
            onClick = { onFilterSelected(TaskStatus.TO_DO) },
            label = { Text("A faire") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = StatusToDo.copy(alpha = 0.2f),
                selectedLabelColor = StatusToDo
            )
        )

        FilterChip(
            selected = selectedFilter == TaskStatus.ON_GOING,
            onClick = { onFilterSelected(TaskStatus.ON_GOING) },
            label = { Text("En cours") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = StatusOnGoing.copy(alpha = 0.2f),
                selectedLabelColor = StatusOnGoing
            )
        )

        FilterChip(
            selected = selectedFilter == TaskStatus.OVERDUE,
            onClick = { onFilterSelected(TaskStatus.OVERDUE) },
            label = { Text("En retard") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = StatusOverdue.copy(alpha = 0.2f),
                selectedLabelColor = StatusOverdue
            )
        )

        FilterChip(
            selected = selectedFilter == TaskStatus.COMPLETED,
            onClick = { onFilterSelected(TaskStatus.COMPLETED) },
            label = { Text("Terminees") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = StatusCompleted.copy(alpha = 0.2f),
                selectedLabelColor = StatusCompleted
            )
        )
    }
}
