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
import com.example.statusboard.home.NotificationScreen
import com.example.statusboard.settings.SettingsScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.statusboard.ui.theme.ThemeMode
import com.example.statusboard.users.ProfileScreen

@Composable
fun AppNavGraph(navController: NavHostController,
                themeMode: ThemeMode,
                onThemeChange: (ThemeMode) -> Unit,
                startDestination: String
) {

    NavHost(
        navController = navController,
        startDestination = startDestination
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
                onOpenNotifications = { navController.navigate("notifications") },
                onAddFriend = { navController.navigate("addFriend") },
                onOpenProfile = { navController.navigate("profile") }
            )

        }


        composable("addFriend") {
            AddFriendScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = "notifications") {
            NotificationScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = "settings") {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onEditNickname = {
                    navController.navigate("nickname")   // keep your existing nickname route
                },
                onOpenProfile = {
                    navController.navigate("profile")
                },
                onLogoutSuccess = {
                    // use your existing route names ("Login", "home") here
                    navController.navigate("Login") {
                        popUpTo("Login") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                currentTheme = themeMode,
                onThemeChange = onThemeChange
            )
        }
        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
