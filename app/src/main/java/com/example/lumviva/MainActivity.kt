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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lumviva.ui.Inicio.ui.InicioScreen
import com.example.lumviva.ui.RecuperarContraseña.ui.RecuperarContraseñaScreen
import com.example.lumviva.ui.Reportes.ui.ReportesScreen
import com.example.lumviva.ui.crearcuenta.ui.CrearCuentaScreen
import com.example.lumviva.ui.login.ui.LoginScreen
import com.example.lumviva.ui.theme.LumVivaTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
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

@Preview
@Composable
fun LumVivaApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "inicio") {
        composable("inicio") {
            InicioScreen(navController = navController)
        }
        composable("reportes") {
            ReportesScreen(navController = navController, userName = "Prueba")
        }
        composable("login") { LoginScreen(navController) }
        composable("recuperar_contrasena") { RecuperarContraseñaScreen(navController) }
        composable("crear_cuenta") { CrearCuentaScreen(navController) }
    }
}