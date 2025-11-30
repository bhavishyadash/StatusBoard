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
import com.example.statusboard.friends.AddFriendScreen

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
                    // after account creation, go to nickname screen
                    navController.navigate("nickname") {
                        popUpTo("login") { inclusive = false }
                        popUpTo("signup") { inclusive = true }
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
            StatusBoardScreen(
                onOpenSettings = { navController.navigate("settings") },
                onAddFriend = { navController.navigate("addFriend") })
        }

        composable("addFriend") {
            AddFriendScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
