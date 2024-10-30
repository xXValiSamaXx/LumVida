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
import com.example.lumviva.ui.PerfilUsuario.PerfilUsuarioScreen
import com.example.lumviva.ui.PerfilUsuario.PerfilUsuarioViewModel
import com.example.lumviva.ui.crearcuenta.CrearCuentaScreen
import com.example.lumviva.ui.login.ui.LoginScreen
import com.example.lumviva.ui.theme.LumVivaTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() { // Clase principal de la actividad que extiende ComponentActivity.
    private val authViewModel: AuthViewModel by viewModels() // Obtiene una instancia del ViewModel de autenticación.

    @RequiresApi(Build.VERSION_CODES.O) // Requiere que la versión de la API sea O o superior.
    override fun onCreate(savedInstanceState: Bundle?) { // Método que se llama al crear la actividad.
        super.onCreate(savedInstanceState) // Llama al método de la clase base.
        FirebaseApp.initializeApp(this) // Inicializa Firebase en la aplicación.
        enableEdgeToEdge() // Habilita el modo de pantalla completa (Edge to Edge).
        setContent { // Establece el contenido de la actividad utilizando Compose.
            LumVivaTheme { // Aplica el tema de la aplicación.
                Surface( // Crea una superficie para la interfaz de usuario.
                    modifier = Modifier.fillMaxSize(), // Modificador para llenar todo el tamaño disponible.
                    color = MaterialTheme.colorScheme.background // Color de fondo según el tema.
                ) {
                    LumVivaApp(authViewModel) // Llama a la función composable LumVivaApp.
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O) // Requiere que la versión de la API sea O o superior.
@Composable // Indica que esta es una función composable.
fun LumVivaApp(authViewModel: AuthViewModel) { // Función principal de la aplicación.
    val navController = rememberNavController() // Crea un controlador de navegación.
    val authState = authViewModel.authState.collectAsState().value // Recoge el estado de autenticación.

    LaunchedEffect(authState) { // Ejecuta efectos secundarios basados en el estado de autenticación.
        when (authState) {
            is AuthState.Authenticated -> { // Si el usuario está autenticado.
                // Si está en login o crear_cuenta, ir a reportes
                val currentRoute = navController.currentDestination?.route // Obtiene la ruta actual.
                if (currentRoute in listOf("login", "crear_cuenta", "inicio")) { // Si está en las rutas específicas.
                    navController.navigate("reportes") { // Navega a la pantalla de reportes.
                        popUpTo(0) { inclusive = true } // Limpia la pila de navegación.
                    }
                }
            }
            else -> {} // No hacer nada para otros estados.
        }
    }

    NavHost( // Define el contenedor de navegación.
        navController = navController, // Asigna el controlador de navegación.
        startDestination = if (authState is AuthState.Authenticated) "reportes" else "inicio" // Define la pantalla de inicio según el estado de autenticación.
    ) {
        composable("inicio") { // Ruta para la pantalla de inicio.
            InicioScreen(navController = navController) // Llama a la pantalla de inicio.
        }
        composable("reportes") { // Ruta para la pantalla de reportes.
            ReportesScreen(
                navController = navController,
                authViewModel = authViewModel // Pasa el ViewModel de autenticación.
            )
        }
        composable("usuario") { // Ruta para la pantalla de perfil de usuario.
            val perfilUsuarioViewModel: PerfilUsuarioViewModel = viewModel( // Obtiene el ViewModel para el perfil.
                factory = PerfilUsuarioViewModel.Factory(authViewModel) // Utiliza una fábrica para el ViewModel.
            )
            PerfilUsuarioScreen( // Llama a la pantalla de perfil.
                navController = navController,
                viewModel = perfilUsuarioViewModel // Pasa el ViewModel de perfil de usuario.
            )
        }
        composable("login") { // Ruta para la pantalla de inicio de sesión.
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel // Pasa el ViewModel de autenticación.
            )
        }
        composable("recuperar_contrasena") { // Ruta para la pantalla de recuperación de contraseña.
            RecuperarContraseñaScreen(
                navController = navController,
                authViewModel = authViewModel // Pasa el ViewModel de autenticación.
            )
        }
        composable("crear_cuenta") { // Ruta para la pantalla de crear cuenta.
            CrearCuentaScreen(
                navController = navController,
                authViewModel = authViewModel // Pasa el ViewModel de autenticación.
            )
        }
        composable("categorias") { // Ruta para la pantalla de categorías.
            val categoriasViewModel: CategoriasViewModel = viewModel() // Obtiene el ViewModel de categorías.
            CategoriasScreen(
                navController = navController,
                viewModel = categoriasViewModel // Pasa el ViewModel de categorías.
            )
        }
        composable( // Ruta para crear un reporte con un argumento.
            route = "crear_reporte/{categoria}", // Define la ruta con un parámetro.
            arguments = listOf( // Define los argumentos de la ruta.
                navArgument("categoria") { // Define el argumento 'categoria'.
                    type = NavType.StringType // Tipo de argumento.
                    defaultValue = "sin categoría" // Valor por defecto.
                }
            )
        ) {
            val crearReporteViewModel: CrearReporteViewModel = viewModel() // Obtiene el ViewModel para crear reportes.
            CrearReporteScreen(
                navController = navController,
                viewModel = crearReporteViewModel, // Pasa el ViewModel de crear reportes.
                authViewModel = authViewModel, // Pasa el ViewModel de autenticación.
                categoria = it.arguments?.getString("categoria") ?: "sin categoría" // Obtiene el argumento 'categoria'.
            )
        }
    }
}
