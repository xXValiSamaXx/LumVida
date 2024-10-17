package com.example.lumviva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lumviva.ui.Inicio.ui.InicioScreen
import com.example.lumviva.ui.RecuperarContraseña.ui.RecuperarContraseñaScreen
import com.example.lumviva.ui.Reportes.ui.ReportesScreen
import com.example.lumviva.ui.auth.AuthViewModel
import com.example.lumviva.ui.auth.AuthState
import com.example.lumviva.ui.crearcuenta.CrearCuentaScreen
import com.example.lumviva.ui.login.ui.LoginScreen
import com.example.lumviva.ui.theme.LumVivaTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

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
                    LumVivaApp(authViewModel)
                }
            }
        }
    }
}

@Composable
fun LumVivaApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState = authViewModel.authState.collectAsState().value

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // Si el usuario está autenticado, navega a la pantalla de reportes
                navController.navigate("reportes") {
                    popUpTo("login") { inclusive = true } // Elimina la pantalla de login del stack de navegación
                }
            }
            else -> {
                // Si no está autenticado, navega a la pantalla de inicio o login
                navController.navigate("inicio") {
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = if (authState is AuthState.Authenticated) "reportes" else "login") {
        composable("inicio") {
            InicioScreen(navController = navController)
        }
        composable("reportes") {
            ReportesScreen(navController = navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("recuperar_contrasena") {
            RecuperarContraseñaScreen(navController)
        }
        composable("crear_cuenta") {
            CrearCuentaScreen(navController)
        }
    }
}
