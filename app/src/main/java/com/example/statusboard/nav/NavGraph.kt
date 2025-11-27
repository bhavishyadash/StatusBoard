package com.example.statusboard.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.statusboard.auth.LoginScreen
import com.example.statusboard.auth.NicknameScreen
import com.example.statusboard.auth.SignupScreen
import com.example.statusboard.data.createFirestoreUserIfNeeded
import com.example.statusboard.home.StatusBoardScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    createFirestoreUserIfNeeded()
                    navController.navigate("nickname") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignupClick = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignupScreen(
                onSignupSuccess = {
                    // account created -> ensure Firestore doc -> nickname
                    createFirestoreUserIfNeeded()
                    navController.navigate("nickname") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("nickname") {
            NicknameScreen(
                onDone = {
                    navController.navigate("home") {
                        popUpTo("nickname") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            StatusBoardScreen()
        }
    }
}
