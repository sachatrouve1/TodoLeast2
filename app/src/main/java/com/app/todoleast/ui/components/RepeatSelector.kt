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
import androidx.compose.ui.unit.dp
import com.app.todoleast.model.Repeat

@Composable
fun RepeatSelector(
    selectedRepeat: Repeat,
    onRepeatSelected: (Repeat) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RepeatOption.entries.forEach { option ->
            FilterChip(
                selected = selectedRepeat == option.repeat,
                onClick = { onRepeatSelected(option.repeat) },
                label = { Text(option.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

private enum class RepeatOption(val repeat: Repeat, val label: String) {
    NONE(Repeat.NONE, "Aucune"),
    DAILY(Repeat.DAILY, "Jour"),
    WEEKLY(Repeat.WEEKLY, "Semaine"),
    MONTHLY(Repeat.MONTHLY, "Mois")
}
