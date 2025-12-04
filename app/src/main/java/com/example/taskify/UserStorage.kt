package com.example.taskify

import android.content.Context
import android.content.SharedPreferences

object UserStorage {

    private const val PREF_NAME = "user_prefs"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Save User
    fun saveUser(context: Context, email: String, password: String) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    // Load User
    fun getUser(context: Context): UserData? {
        val prefs = getPrefs(context)

        val email = prefs.getString(KEY_EMAIL, null)
        val password = prefs.getString(KEY_PASSWORD, null)

        return if (email != null && password != null) {
            UserData(email, password)
        } else {
            null
        }
    }
}

// Simple data holder
data class UserData(
    val email: String,
    val password: String
)
