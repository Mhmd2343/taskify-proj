package com.example.taskify.ui.admin

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

object FirebaseSecondaryAuth {

    private const val SECONDARY_APP_NAME = "SecondaryAuth"

    private fun getSecondaryAuth(context: Context): FirebaseAuth {
        val existing = FirebaseApp.getApps(context).firstOrNull { it.name == SECONDARY_APP_NAME }

        val secondaryApp = if (existing != null) {
            existing
        } else {
            val defaultApp = FirebaseApp.getInstance()
            FirebaseApp.initializeApp(context, defaultApp.options, SECONDARY_APP_NAME)
                ?: throw IllegalStateException("Failed to initialize secondary FirebaseApp")
        }

        return FirebaseAuth.getInstance(secondaryApp)
    }

    fun createUserWithoutAffectingAdmin(
        context: Context,
        email: String,
        password: String
    ): String {
        val secondaryAuth = getSecondaryAuth(context)

        val task = secondaryAuth.createUserWithEmailAndPassword(email, password)
        Tasks.await(task)

        val uid = secondaryAuth.currentUser?.uid
            ?: throw IllegalStateException("Teacher UID is null after account creation")

        secondaryAuth.signOut()
        return uid
    }
}
