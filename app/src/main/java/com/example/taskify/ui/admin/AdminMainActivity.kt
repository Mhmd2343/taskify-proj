package com.example.taskify.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskify.ui.admin.nav.AdminTab
import com.example.taskify.ui.admin.screens.AdminDashboardScreen
import com.example.taskify.ui.admin.screens.AdminProfileScreen
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

class AdminMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AdminRoot(
                    onCreateTeacher = { startActivity(Intent(this, CreateTeacherActivity::class.java)) },
                    onTeacherList = { startActivity(Intent(this, TeacherListActivity::class.java)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRoot(
    onCreateTeacher: () -> Unit,
    onTeacherList: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var tab: AdminTab by remember { mutableStateOf(AdminTab.Home) }

    fun closeDrawer() {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Admin Menu",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Add Teacher") },
                    icon = { Icon(Icons.Default.PersonAdd, null) },
                    selected = false,
                    onClick = {
                        closeDrawer()
                        onCreateTeacher()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Teacher List") },
                    icon = { Icon(Icons.Default.People, null) },
                    selected = false,
                    onClick = {
                        closeDrawer()
                        onTeacherList()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Student Grades") },
                    icon = { Icon(Icons.Default.Grade, null) },
                    selected = false,
                    onClick = { closeDrawer() }
                )

                NavigationDrawerItem(
                    label = { Text("All Tasks (Read-only)") },
                    icon = { Icon(Icons.Default.List, null) },
                    selected = false,
                    onClick = { closeDrawer() }
                )

                NavigationDrawerItem(
                    label = { Text("Logout") },
                    icon = { Icon(Icons.Default.Logout, null) },
                    selected = false,
                    onClick = {
                        closeDrawer()
                        tab = AdminTab.Profile
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (tab) {
                                AdminTab.Home -> "Admin Dashboard"
                                AdminTab.Teachers -> "Teachers"
                                AdminTab.Profile -> "Profile"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == AdminTab.Home,
                        onClick = { tab = AdminTab.Home },
                        icon = { Icon(AdminTab.Home.icon, null) },
                        label = { Text(AdminTab.Home.label) }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = { onTeacherList() },
                        icon = { Icon(AdminTab.Teachers.icon, null) },
                        label = { Text(AdminTab.Teachers.label) }
                    )


                    NavigationBarItem(
                        selected = tab == AdminTab.Profile,
                        onClick = { tab = AdminTab.Profile },
                        icon = { Icon(AdminTab.Profile.icon, null) },
                        label = { Text(AdminTab.Profile.label) }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (tab) {
                    AdminTab.Home -> AdminDashboardScreen(
                        onCreateTeacher = onCreateTeacher,
                        onTeacherList = onTeacherList,
                        onStudentGrades = {}
                    )

                    AdminTab.Teachers -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Open Teachers List from here")
                    }

                    AdminTab.Profile -> AdminProfileScreen(
                        onPasswordUpdated = { tab = AdminTab.Home }
                    )
                }

            }
        }
    }
}
