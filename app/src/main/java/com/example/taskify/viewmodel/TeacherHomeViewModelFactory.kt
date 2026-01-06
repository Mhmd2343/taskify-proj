package com.example.taskify.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TeacherHomeViewModelFactory(
    private val app: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TeacherHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TeacherHomeViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
