package com.example.taskify.ui.student

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskify.ui.student.screens.StudentAidCalculatorScreen
import com.example.taskify.ui.student.screens.StudentChooseSubjectsScreen
import com.example.taskify.ui.student.screens.StudentSubjectFeedScreen
import com.example.taskify.ui.student.screens.StudentSubjectsScreen

object StudentRoutes {
    const val ChooseSubjects = "student_choose_subjects"
    const val Subjects = "student_subjects"
    const val SubjectFeed = "student_subject_feed/{subjectName}"
    const val AidCalculator = "student_aid_calculator"
}

@Composable
fun StudentNav(modifier: Modifier = Modifier) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = StudentRoutes.ChooseSubjects,
        modifier = modifier
    ) {
        composable(StudentRoutes.ChooseSubjects) {
            StudentChooseSubjectsScreen(
                onContinue = { nav.navigate(StudentRoutes.Subjects) }
            )
        }

        composable(StudentRoutes.Subjects) {
            StudentSubjectsScreen(
                onOpenSubject = { subjectName ->
                    nav.navigate("student_subject_feed/$subjectName")
                },
                onOpenAidCalculator = {
                    nav.navigate(StudentRoutes.AidCalculator)
                }
            )
        }

        composable(StudentRoutes.AidCalculator) {
            StudentAidCalculatorScreen()
        }

        composable(
            route = StudentRoutes.SubjectFeed,
            arguments = listOf(navArgument("subjectName") { type = NavType.StringType })
        ) { backStackEntry ->
            val subjectName = backStackEntry.arguments?.getString("subjectName").orEmpty()

            StudentSubjectFeedScreen(
                subjectName = subjectName,
                onOpenTask = { taskId ->
                }
            )
        }
    }
}
