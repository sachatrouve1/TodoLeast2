package com.app.todoleast.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.todoleast.ui.screens.AddTaskScreen
import com.app.todoleast.ui.screens.EditTaskScreen
import com.app.todoleast.ui.screens.TaskListScreen
import com.app.todoleast.viewmodel.TaskViewModel

sealed class Screen(val route: String) {
    data object TaskList : Screen("task_list")
    data object AddTask : Screen("add_task")
    data object EditTask : Screen("edit_task/{taskId}") {
        fun createRoute(taskId: String) = "edit_task/$taskId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: TaskViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.TaskList.route
    ) {
        composable(Screen.TaskList.route) {
            TaskListScreen(
                viewModel = viewModel,
                onAddTaskClick = {
                    navController.navigate(Screen.AddTask.route)
                },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.EditTask.createRoute(taskId))
                },
                onToggleTaskCompletion = { taskId, position ->
                    viewModel.toggleTaskCompletion(taskId, position)
                }
            )
        }

        composable(Screen.AddTask.route) {
            AddTaskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTaskCreated = { title, description, dueDate, dueTime, repeat, priority, photoUri ->
                    viewModel.addTask(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        dueTime = dueTime,
                        repeat = repeat,
                        priority = priority,
                        photoUri = photoUri
                    )
                }
            )
        }

        composable(
            route = Screen.EditTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            val task = viewModel.getTaskById(taskId) ?: return@composable

            EditTaskScreen(
                task = task,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTaskUpdated = { title, description, dueDate, dueTime, repeat, priority, photoUri ->
                    viewModel.updateTask(
                        taskId = taskId,
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        dueTime = dueTime,
                        repeat = repeat,
                        priority = priority,
                        photoUri = photoUri
                    )
                },
                onDeleteTask = {
                    viewModel.deleteTask(taskId)
                }
            )
        }
    }
}
