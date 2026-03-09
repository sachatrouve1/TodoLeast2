package com.app.todoleast.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
                icon = option.icon,
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
    icon: ImageVector?,
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

    val contentColor by animateColorAsState(
        targetValue = if (selected)
            MaterialTheme.colorScheme.onTertiaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "repeatContent"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor
        )
    }
}

private enum class RepeatOption(
    val repeat: Repeat,
    val label: String,
    val icon: ImageVector?
) {
    NONE(Repeat.NONE, "Aucune", Icons.Outlined.Block),
    DAILY(Repeat.DAILY, "Jour", Icons.Outlined.WbSunny),
    WEEKLY(Repeat.WEEKLY, "Semaine", Icons.Outlined.DateRange),
    MONTHLY(Repeat.MONTHLY, "Mois", Icons.Outlined.CalendarMonth)
}
