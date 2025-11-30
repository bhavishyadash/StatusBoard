package com.example.statusboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.example.statusboard.nav.AppNavGraph
import com.example.statusboard.ui.theme.StatusBoardTheme
import com.example.statusboard.ui.theme.ThemeMode
import com.example.statusboard.ui.theme.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth   // <– add this

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()

            // 🔑 Decide where to start based on whether user is already logged in
            val auth = FirebaseAuth.getInstance()
            val startDestination = if (auth.currentUser != null) {
                "home"      // your StatusBoard route
            } else {
                "Login"     // your login route (capital L from your NavGraph)
            }

            StatusBoardTheme(themeMode = themeMode) {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    themeMode = themeMode,
                    onThemeChange = { newMode -> themeViewModel.setTheme(newMode) },
                    startDestination = startDestination   // 👈 NEW
                )
            }
        }
    }
}