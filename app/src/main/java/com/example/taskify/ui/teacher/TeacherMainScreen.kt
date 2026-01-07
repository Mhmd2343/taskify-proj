package com.example.taskify.ui.teacher

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskify.ui.LoginActivity
import com.example.taskify.ui.teacher.screens.TeacherAddTasksScreen
import com.example.taskify.ui.teacher.screens.TeacherHomeScreen
import com.example.taskify.ui.teacher.screens.TeacherProfileScreen
import com.example.taskify.ui.teacher.screens.TeacherStudentsScreen
import com.example.taskify.ui.teacher.screens.TeacherTaskDetailsScreen
import com.example.taskify.ui.teacher.screens.TeacherTasksListScreen
import com.example.taskify.viewmodel.TeacherHomeViewModel
import com.example.taskify.viewmodel.TeacherHomeViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

enum class TeacherTab { Home, Profile, Tasks, Students }
enum class TeacherTasksRoute { List, Add, Details }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMainScreen() {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(TeacherTab.Home) }

    fun doLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
        (context as? Activity)?.finish()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Teacher Actions")

                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = selectedTab == TeacherTab.Home,
                    onClick = {
                        selectedTab = TeacherTab.Home
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) }
                )

                NavigationDrawerItem(
                    label = { Text("Show My Students") },
                    selected = selectedTab == TeacherTab.Students,
                    onClick = {
                        selectedTab = TeacherTab.Students
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.People, contentDescription = null) }
                )

                NavigationDrawerItem(
                    label = { Text("Create / View Tasks") },
                    selected = selectedTab == TeacherTab.Tasks,
                    onClick = {
                        selectedTab = TeacherTab.Tasks
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Assignment, contentDescription = null) }
                )

                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = selectedTab == TeacherTab.Profile,
                    onClick = {
                        selectedTab = TeacherTab.Profile
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) }
                )

                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        doLogout()
                    },
                    icon = { Icon(Icons.Filled.Logout, contentDescription = null) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Teacher") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { doLogout() }) {
                            Icon(Icons.Filled.Logout, contentDescription = "Logout")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == TeacherTab.Home,
                        onClick = { selectedTab = TeacherTab.Home },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == TeacherTab.Profile,
                        onClick = { selectedTab = TeacherTab.Profile },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == TeacherTab.Tasks,
                        onClick = { selectedTab = TeacherTab.Tasks },
                        icon = { Icon(Icons.Filled.Assignment, contentDescription = "Tasks") },
                        label = { Text("Tasks") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == TeacherTab.Students,
                        onClick = { selectedTab = TeacherTab.Students },
                        icon = { Icon(Icons.Filled.People, contentDescription = "Students") },
                        label = { Text("Students") }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val vm: TeacherHomeViewModel = viewModel(
                    factory = TeacherHomeViewModelFactory(context.applicationContext as Application)
                )

                when (selectedTab) {
                    TeacherTab.Home -> TeacherHomeScreen(vm, onShowStudents = { selectedTab = TeacherTab.Students })

                    TeacherTab.Profile -> TeacherProfileScreen(
                        onLogout = { doLogout() }
                    )

                    TeacherTab.Tasks -> {
                        var tasksRoute by remember { mutableStateOf(TeacherTasksRoute.List) }
                        var selectedTaskId by remember { mutableStateOf<String?>(null) }

                        when (tasksRoute) {
                            TeacherTasksRoute.List -> {
                                TeacherTasksListScreen(
                                    onCreateTask = { tasksRoute = TeacherTasksRoute.Add },
                                    onOpenTask = { taskId ->
                                        selectedTaskId = taskId
                                        tasksRoute = TeacherTasksRoute.Details
                                    }
                                )
                            }

                            TeacherTasksRoute.Add -> {
                                TeacherAddTasksScreen(
                                    onBack = { tasksRoute = TeacherTasksRoute.List }
                                )
                            }

                            TeacherTasksRoute.Details -> {
                                TeacherTaskDetailsScreen(
                                    taskId = selectedTaskId ?: "",
                                    onBack = { tasksRoute = TeacherTasksRoute.List }
                                )
                            }
                        }
                    }

                    TeacherTab.Students -> TeacherStudentsScreen(vm)
                }
            }
        }
    }
}
