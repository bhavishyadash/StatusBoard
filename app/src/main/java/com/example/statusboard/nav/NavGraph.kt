package com.example.statusboard.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.statusboard.data.createFirestoreUserIfNeeded
import com.example.statusboard.ui.auth.LoginScreen
import com.example.statusboard.ui.home.StatusBoardScreen

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        // LOGIN SCREEN
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    createFirestoreUserIfNeeded()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // STATUS BOARD
        composable("home") {
            StatusBoardScreen()
        }
    }
}
