package com.app.todoleast.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import com.app.todoleast.service.NotificationHelper
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Tick every minute to refresh overdue status in real-time
    var refreshTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L) // Update every minute
            refreshTick++
        }
    }

    val filteredTasks = remember(tasks, selectedFilter, refreshTick) {
        viewModel.getFilteredTasks()
    }
    val dailyTasks = remember(tasks, selectedFilter, refreshTick) {
        viewModel.getFilteredPeriodicTasks(Repeat.DAILY, selectedFilter)
    }
    val weeklyTasks = remember(tasks, selectedFilter, refreshTick) {
        viewModel.getFilteredPeriodicTasks(Repeat.WEEKLY, selectedFilter)
    }
    val monthlyTasks = remember(tasks, selectedFilter, refreshTick) {
        viewModel.getFilteredPeriodicTasks(Repeat.MONTHLY, selectedFilter)
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showDeleteCompletedDialog by remember { mutableStateOf(false) }
    var showRewardsDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val hasCompletedTasks = remember(tasks) {
        tasks.any { it.status == TaskStatus.COMPLETED && !it.isPeriodic() }
    }

    // Notification check - tracking is done in ViewModel to persist across navigation
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val notificationHelper = NotificationHelper(context)

        while (true) {
            // Check for new overdue tasks
            val newOverdueTasks = viewModel.getNewOverdueTasks()
            if (newOverdueTasks.isNotEmpty()) {
                notificationHelper.showOverdueNotification(newOverdueTasks)
            }

            // Check for new tasks due soon (within 30 minutes)
            val newDueSoonTasks = viewModel.getNewDueSoonTasks(30)
            if (newDueSoonTasks.isNotEmpty()) {
                notificationHelper.showDueSoonNotification(newDueSoonTasks)
            }

            // Check for new uncompleted periodic tasks
            val newMissedPeriodic = viewModel.getNewMissedPeriodicTasks()
            if (newMissedPeriodic.isNotEmpty()) {
                notificationHelper.showMissedPeriodicNotification(newMissedPeriodic)
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
                                text = "${tasks.size} tâche${if (tasks.size > 1) "s" else ""}",
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
                                contentDescription = "Supprimer les tâches terminées",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Plus d'options"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Exporter les tâches") },
                                onClick = {
                                    showMenu = false
                                    val json = viewModel.exportTasksJson()
                                    clipboardManager.setText(AnnotatedString(json))
                                    Toast.makeText(context, "Taches copiees dans le presse-papiers", Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.FileUpload,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Importer des tâches") },
                                onClick = {
                                    showMenu = false
                                    showImportDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.FileDownload,
                                        contentDescription = null
                                    )
                                }
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
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter une tâche",
                    modifier = Modifier.size(28.dp)
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
                title = { Text("Supprimer les tâches terminées") },
                text = { Text("Voulez-vous vraiment supprimer toutes les tâches terminées ?") },
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

        // Import dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = {
                    showImportDialog = false
                    importText = ""
                },
                title = { Text("Importer des tâches") },
                text = {
                    Column {
                        Text(
                            text = "Collez le JSON exporte depuis une autre instance de l'application:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = importText,
                            onValueChange = { importText = it },
                            label = { Text("JSON") },
                            modifier = Modifier.height(200.dp),
                            maxLines = 10
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val success = viewModel.importTasksJson(importText)
                            if (success) {
                                Toast.makeText(context, "Taches importees avec succes", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Erreur lors de l'importation", Toast.LENGTH_SHORT).show()
                            }
                            showImportDialog = false
                            importText = ""
                        },
                        enabled = importText.isNotBlank()
                    ) {
                        Text("Importer")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showImportDialog = false
                            importText = ""
                        }
                    ) {
                        Text("Annuler")
                    }
                }
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
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Aucune tâche",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Appuyez sur + pour créer\nvotre première tâche",
                style = MaterialTheme.typography.bodyLarge,
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
        TaskStatus.COMPLETED -> "terminée"
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
                text = "Aucune tâche $filterName",
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
        modifier = modifier
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleSmall
            )
        }
        Spacer(modifier = Modifier.padding(horizontal = 6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 0.5.sp
        )
    }
}
