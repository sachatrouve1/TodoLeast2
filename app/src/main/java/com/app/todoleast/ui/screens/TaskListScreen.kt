package com.app.todoleast.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.app.todoleast.service.NotificationHelper
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.todoleast.model.Repeat
import com.app.todoleast.model.Task
import com.app.todoleast.model.TaskStatus
import com.app.todoleast.ui.components.AchievementUnlockedDialog
import com.app.todoleast.ui.components.CelebrationEffect
import com.app.todoleast.ui.components.FilterChips
import com.app.todoleast.ui.components.PointsBadge
import com.app.todoleast.ui.components.RewardsDialog
import com.app.todoleast.ui.components.TaskItem
import com.app.todoleast.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onAddTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onToggleTaskCompletion: (String, Offset) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val celebrationState by viewModel.celebrationState.collectAsState()
    val rewardsState by viewModel.rewardsState.collectAsState()
    val filteredTasks = remember(tasks, selectedFilter) {
        viewModel.getFilteredTasks()
    }
    val dailyTasks = remember(tasks, selectedFilter) {
        viewModel.getFilteredPeriodicTasks(Repeat.DAILY, selectedFilter)
    }
    val weeklyTasks = remember(tasks, selectedFilter) {
        viewModel.getFilteredPeriodicTasks(Repeat.WEEKLY, selectedFilter)
    }
    val monthlyTasks = remember(tasks, selectedFilter) {
        viewModel.getFilteredPeriodicTasks(Repeat.MONTHLY, selectedFilter)
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showDeleteCompletedDialog by remember { mutableStateOf(false) }
    var showRewardsDialog by remember { mutableStateOf(false) }
    val hasCompletedTasks = remember(tasks) {
        tasks.any { it.status == TaskStatus.COMPLETED && !it.isPeriodic() }
    }

    // Notification check
    val context = LocalContext.current
    LaunchedEffect(tasks) {
        val notificationHelper = NotificationHelper(context)

        while (true) {
            // Check for overdue tasks
            val overdueTasks = viewModel.getOverdueTasks()
            if (overdueTasks.isNotEmpty()) {
                notificationHelper.showOverdueNotification(overdueTasks)
            }

            // Check for tasks due soon (within 30 minutes)
            val dueSoonTasks = viewModel.getTasksDueSoon(30)
            if (dueSoonTasks.isNotEmpty()) {
                notificationHelper.showDueSoonNotification(dueSoonTasks)
            }

            // Check for uncompleted periodic tasks
            val missedPeriodicTasks = viewModel.getUncompletedPeriodicTasks()
            if (missedPeriodicTasks.isNotEmpty()) {
                notificationHelper.showMissedPeriodicNotification(missedPeriodicTasks)
            }

            // Check every minute
            delay(60_000L)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TodoLeast",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (tasks.isNotEmpty()) {
                            Text(
                                text = "${tasks.size} tache${if (tasks.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    PointsBadge(
                        points = rewardsState.totalPoints,
                        onClick = { showRewardsDialog = true }
                    )
                    if (hasCompletedTasks) {
                        IconButton(onClick = { showDeleteCompletedDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Supprimer les taches terminees",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter une tache"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            if (tasks.isNotEmpty()) {
                FilterChips(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { viewModel.setFilter(it) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            AnimatedVisibility(
                visible = tasks.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyState(modifier = Modifier.fillMaxSize())
            }

            AnimatedVisibility(
                visible = tasks.isNotEmpty() && filteredTasks.isEmpty() && dailyTasks.isEmpty() && weeklyTasks.isEmpty() && monthlyTasks.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyFilterState(
                    filter = selectedFilter,
                    modifier = Modifier.fillMaxSize()
                )
            }

            AnimatedVisibility(
                visible = tasks.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TaskListWithSections(
                    regularTasks = filteredTasks,
                    dailyTasks = dailyTasks,
                    weeklyTasks = weeklyTasks,
                    monthlyTasks = monthlyTasks,
                    onTaskClick = onTaskClick,
                    onToggleTaskCompletion = onToggleTaskCompletion
                )
            }
        }
    }

        // Celebration effect overlay
        CelebrationEffect(
            show = celebrationState.task != null,
            startPosition = celebrationState.position,
            onAnimationComplete = { viewModel.clearCelebration() }
        )

        // Delete completed tasks confirmation dialog
        if (showDeleteCompletedDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteCompletedDialog = false },
                title = { Text("Supprimer les taches terminees") },
                text = { Text("Voulez-vous vraiment supprimer toutes les taches terminees ?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteCompletedTasks()
                            showDeleteCompletedDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Supprimer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteCompletedDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }

        // Rewards dialog
        if (showRewardsDialog) {
            RewardsDialog(
                rewardsState = rewardsState,
                onDismiss = { showRewardsDialog = false }
            )
        }

        // Achievement unlocked dialog
        rewardsState.newlyUnlockedAchievement?.let { achievement ->
            AchievementUnlockedDialog(
                achievement = achievement,
                onDismiss = { viewModel.clearNewAchievement() }
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Aucune tache",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Appuyez sur + pour creer\nvotre premiere tache",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyFilterState(
    filter: TaskStatus?,
    modifier: Modifier = Modifier
) {
    val filterName = when (filter) {
        TaskStatus.TO_DO -> "a faire"
        TaskStatus.COMPLETED -> "terminee"
        TaskStatus.OVERDUE -> "en retard"
        null -> ""
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Aucune tache $filterName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TaskListWithSections(
    regularTasks: List<Task>,
    dailyTasks: List<Task>,
    weeklyTasks: List<Task>,
    monthlyTasks: List<Task>,
    onTaskClick: (String) -> Unit,
    onToggleTaskCompletion: (String, Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Daily tasks section
        if (dailyTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Quotidien", emoji = "📅")
            }
            items(
                items = dailyTasks,
                key = { "daily_${it.id}" }
            ) { task ->
                TaskItem(
                    task = task,
                    onTaskClick = { onTaskClick(task.id) },
                    onToggleCompletion = { position -> onToggleTaskCompletion(task.id, position) }
                )
            }
        }

        // Weekly tasks section
        if (weeklyTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Hebdomadaire", emoji = "📆")
            }
            items(
                items = weeklyTasks,
                key = { "weekly_${it.id}" }
            ) { task ->
                TaskItem(
                    task = task,
                    onTaskClick = { onTaskClick(task.id) },
                    onToggleCompletion = { position -> onToggleTaskCompletion(task.id, position) }
                )
            }
        }

        // Monthly tasks section
        if (monthlyTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Mensuel", emoji = "🗓️")
            }
            items(
                items = monthlyTasks,
                key = { "monthly_${it.id}" }
            ) { task ->
                TaskItem(
                    task = task,
                    onTaskClick = { onTaskClick(task.id) },
                    onToggleCompletion = { position -> onToggleTaskCompletion(task.id, position) }
                )
            }
        }

        // Regular tasks section
        if (regularTasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Taches", emoji = "📝")
            }
            items(
                items = regularTasks,
                key = { "regular_${it.id}" }
            ) { task ->
                TaskItem(
                    task = task,
                    onTaskClick = { onTaskClick(task.id) },
                    onToggleCompletion = { position -> onToggleTaskCompletion(task.id, position) }
                )
            }
        }

        // Bottom spacing for FAB
        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
