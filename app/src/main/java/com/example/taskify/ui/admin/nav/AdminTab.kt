package com.example.taskify.ui.admin.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AdminTab(
    val label: String,
    val icon: ImageVector
) {
    data object Home : AdminTab("Home", Icons.Default.Home)
    data object Teachers : AdminTab("Teachers", Icons.Default.People)
    data object Profile : AdminTab("Profile", Icons.Default.Person)
}
