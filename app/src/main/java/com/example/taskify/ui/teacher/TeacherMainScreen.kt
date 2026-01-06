package com.example.taskify.ui.teacher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskify.ui.teacher.screens.TeacherHomeScreen
import com.example.taskify.ui.teacher.screens.TeacherProfileScreen
import com.example.taskify.viewmodel.TeacherHomeViewModel
import kotlinx.coroutines.launch
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskify.viewmodel.TeacherHomeViewModelFactory


enum class TeacherTab { Home, Profile, More }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMainScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(TeacherTab.Home) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menu")
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = selectedTab == TeacherTab.Home,
                    onClick = {
                        selectedTab = TeacherTab.Home
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = selectedTab == TeacherTab.Profile,
                    onClick = {
                        selectedTab = TeacherTab.Profile
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("More") },
                    selected = selectedTab == TeacherTab.More,
                    onClick = {
                        selectedTab = TeacherTab.More
                        scope.launch { drawerState.close() }
                    }
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
                        selected = selectedTab == TeacherTab.More,
                        onClick = { selectedTab = TeacherTab.More },
                        icon = { Icon(Icons.Filled.List, contentDescription = "More") },
                        label = { Text("More") }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (selectedTab) {
                    TeacherTab.Home -> {
                        val context = LocalContext.current
                        val vm: com.example.taskify.viewmodel.TeacherHomeViewModel = viewModel(
                            factory = TeacherHomeViewModelFactory(context.applicationContext as Application)
                        )
                        TeacherHomeScreen(vm)

                    }
                    TeacherTab.Profile -> TeacherProfileScreen()
                    TeacherTab.More -> Text("More (we will add tasks/prompts here)")
                }
            }
        }
    }
}
