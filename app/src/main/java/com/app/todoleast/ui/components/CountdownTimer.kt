package com.app.todoleast.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Duration

@Composable
fun CountdownTimer(
    remainingDuration: Duration?,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false
) {
    var remainingSeconds by remember(remainingDuration) {
        mutableLongStateOf(remainingDuration?.seconds ?: 0L)
    }

    LaunchedEffect(remainingDuration) {
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
    }

    val formattedTime = formatDuration(remainingSeconds)
    val timerColor = when {
        isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        remainingSeconds < 3600 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    val prefix = if (isCompleted) "Reset dans " else ""

    Row(
        modifier = modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Timer,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = timerColor
        )
        Text(
            text = prefix + formattedTime,
            style = MaterialTheme.typography.labelMedium,
            color = timerColor,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

private fun formatDuration(totalSeconds: Long): String {
    if (totalSeconds <= 0) return "Termine"

    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        days > 0 -> "${days}j ${hours}h ${minutes}m"
        hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
