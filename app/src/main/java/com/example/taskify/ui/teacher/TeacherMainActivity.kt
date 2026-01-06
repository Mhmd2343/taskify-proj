package com.example.taskify.ui.teacher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.taskify.ui.theme.TaskifyTheme

class TeacherMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskifyTheme {
                TeacherMainScreen()
            }
        }
    }
}
