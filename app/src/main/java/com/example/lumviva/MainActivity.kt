package com.example.lumviva

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lumviva.ui.Inicio.ui.InicioScreen
import com.example.lumviva.ui.RecuperarContraseña.ui.RecuperarContraseñaScreen
import com.example.lumviva.ui.Reportes.ui.ReportesScreen
import com.example.lumviva.ui.Auth.ui.AuthViewModel
import com.example.lumviva.ui.Auth.ui.AuthState
import com.example.lumviva.ui.Categorias.ui.CategoriasScreen
import com.example.lumviva.ui.Categorias.ui.CategoriasViewModel
import com.example.lumviva.ui.CrearReporte.ui.CrearReporteScreen
import com.example.lumviva.ui.CrearReporte.ui.CrearReporteViewModel
import com.example.lumviva.ui.crearcuenta.CrearCuentaScreen
import com.example.lumviva.ui.login.ui.LoginScreen
import com.example.lumviva.ui.theme.LumVivaTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
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
        composable("categorias") {
            val categoriasViewModel: CategoriasViewModel = viewModel()
            CategoriasScreen(
                navController = navController,
                viewModel = categoriasViewModel
            )
        }
        composable(
            route = "crear_reporte/{categoria}",
            arguments = listOf(
                navArgument("categoria") {
                    type = NavType.StringType
                    defaultValue = "sin categoría"
                }
            )
        ) {
            val crearReporteViewModel: CrearReporteViewModel = viewModel()
            CrearReporteScreen(
                navController = navController,
                viewModel = crearReporteViewModel,
                authViewModel = authViewModel,
                categoria = it.arguments?.getString("categoria") ?: "sin categoría"
            )
        }
    }
}