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
                // Si está en login o crear_cuenta, ir a reportes
                val currentRoute = navController.currentDestination?.route
                if (currentRoute in listOf("login", "crear_cuenta", "inicio")) {
                    navController.navigate("reportes") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {} // No hacer nada para otros estados
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authState is AuthState.Authenticated) "reportes" else "inicio"
    ) {
        composable("inicio") {
            InicioScreen(navController = navController)
        }
        composable("reportes") {
            ReportesScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("login") {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("recuperar_contrasena") {
            RecuperarContraseñaScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("crear_cuenta") {
            CrearCuentaScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}