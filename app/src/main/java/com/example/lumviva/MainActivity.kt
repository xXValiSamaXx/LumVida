package com.example.lumviva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lumviva.ui.Inicio.ui.InicioScreen
import com.example.lumviva.ui.theme.LumVivaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumVivaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LumVivaApp()
                }
            }
        }
    }
}

@Composable
fun LumVivaApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "inicio") {
        composable("inicio") {
            InicioScreen(navController = navController)
        }
        composable("reportes") {
            // Aquí irá tu ReportesScreen cuando la crees
            // Por ahora, podemos poner un placeholder
            Surface(modifier = Modifier.fillMaxSize()) {
                // Placeholder para ReportesScreen
            }
        }
    }
}