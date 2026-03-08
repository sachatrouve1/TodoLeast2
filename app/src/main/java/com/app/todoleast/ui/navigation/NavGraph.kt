package com.app.todoleast.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.todoleast.ui.screens.AddTaskScreen
import com.app.todoleast.ui.screens.TaskListScreen
import com.app.todoleast.viewmodel.TaskViewModel

sealed class Screen(val route: String) {
    data object TaskList : Screen("task_list")
    data object AddTask : Screen("add_task")
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
                }
            )
        }

        composable(Screen.AddTask.route) {
            AddTaskScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTaskCreated = { title, description, dueDate, dueTime ->
                    viewModel.addTask(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        dueTime = dueTime
                    )
                }
            )
        }
    }
}
