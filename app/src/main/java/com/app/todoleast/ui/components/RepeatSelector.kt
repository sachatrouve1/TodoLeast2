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
import com.app.todoleast.model.Repeat

@Composable
fun RepeatSelector(
    selectedRepeat: Repeat,
    onRepeatSelected: (Repeat) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RepeatOption.entries.forEach { option ->
            RepeatChip(
                label = option.label,
                emoji = option.emoji,
                selected = selectedRepeat == option.repeat,
                onClick = { onRepeatSelected(option.repeat) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RepeatChip(
    label: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.tertiaryContainer
        else
            Color.Transparent,
        animationSpec = tween(200),
        label = "repeatBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.tertiary
        else
            MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(200),
        label = "repeatBorder"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.onTertiaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "repeatText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$emoji $label",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor
        )
    }
}

private enum class RepeatOption(val repeat: Repeat, val label: String, val emoji: String) {
    NONE(Repeat.NONE, "Aucune", ""),
    DAILY(Repeat.DAILY, "Jour", "📅"),
    WEEKLY(Repeat.WEEKLY, "Semaine", "📆"),
    MONTHLY(Repeat.MONTHLY, "Mois", "🗓️")
}
