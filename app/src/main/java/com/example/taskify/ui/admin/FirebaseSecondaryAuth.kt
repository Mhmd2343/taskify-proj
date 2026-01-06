package com.example.taskify.ui.admin

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth

object FirebaseSecondaryAuth {

    private const val SECONDARY_APP_NAME = "taskify_secondary"

    private fun getSecondaryAuth(context: Context): FirebaseAuth {
        val primary = FirebaseApp.getInstance()
        val existing = runCatching { FirebaseApp.getInstance(SECONDARY_APP_NAME) }.getOrNull()
        val secondaryApp = existing ?: FirebaseApp.initializeApp(
            context,
            FirebaseOptions.Builder(primary.options).build(),
            SECONDARY_APP_NAME
        )
        return FirebaseAuth.getInstance(secondaryApp)
    }

    fun createUserWithoutAffectingAdmin(context: Context, email: String, password: String) {
        val secondaryAuth = getSecondaryAuth(context)
        val task = secondaryAuth.createUserWithEmailAndPassword(email, password)
        Tasks.await(task)
        secondaryAuth.signOut()
    }
}
