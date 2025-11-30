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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()

            StatusBoardTheme(themeMode = themeMode) {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    themeMode = themeMode,
                    onThemeChange = { newMode: ThemeMode ->
                        themeViewModel.setTheme(newMode)
                    }
                )
            }
        }
    }
}