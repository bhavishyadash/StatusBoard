package com.example.statusboard   // keep this if it's your package

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.statusboard.nav.AppNavGraph
import com.example.statusboard.ui.theme.StatusBoardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StatusBoardTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}