/*el usuario puede interactuar con las funciones de hacer un reporte, ver los
reportes previos y acceder a un mapa con incidencias urbanas. Si el usuario no
 está autenticado, se le solicita iniciar sesión a través de un cuadro de diálogo.
  El TopAppBar muestra un mensaje de bienvenida dependiendo del estado de autenticación
  del usuario. Además, se proporcionan botones para hacer un reporte, ver los reportes
   del usuario y acceder al mapa. Un FloatingActionButton permite acceder al mapa de
   incidencias, pero solo si el usuario está autenticado. Si no lo está, el sistema solicita
    el inicio de sesión.*/

package com.example.lumvida.ui.Reportes.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lumvida.R
import com.example.lumvida.ui.Auth.ui.AuthViewModel
import com.example.lumvida.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    reportesViewModel: ReportesViewModel = viewModel(
        factory = ReportesViewModel.Factory(authViewModel)
    ),
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val userName by reportesViewModel.userName.collectAsState()
    val isAuthenticated by reportesViewModel.isAuthenticated.collectAsState()
    var showLoginDialog by remember { mutableStateOf(false) }
    var showingDialogFor by remember { mutableStateOf<String?>(null) }

    // Diálogo de inicio de sesión
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = {
                showLoginDialog = false
                showingDialogFor = null
            },
            title = {
                Text(
                    "Iniciar sesión requerido",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            },
            text = {
                Text(
                    when (showingDialogFor) {
                        "map" -> "Para ver el mapa de incidencias necesitas iniciar sesión."
                        else -> "Para ver tus reportes necesitas iniciar sesión."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLoginDialog = false
                        showingDialogFor = null
                        navController.navigate("login")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        "Iniciar sesión",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLoginDialog = false
                        showingDialogFor = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                ) {
                    Text(
                        "Cancelar",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            containerColor = if (isDarkTheme) PrimaryDark else Color.White
        )
    }

    BackgroundContainer(isDarkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = if (isAuthenticated) "Bienvenido, $userName" else "Bienvenido Invitado",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                },
                actions = {
                    if (isAuthenticated) {
                        IconButton(
                            onClick = { navController.navigate("usuario") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil de usuario",
                                tint = if (isDarkTheme) TextPrimary else PrimaryDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Haz un reporte",
                    style = MaterialTheme.typography.displaySmall,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Si has presenciado o experimentado un problema urbano, como alumbrado público, drenajes obstruidos, mal estado de las calles o situaciones de riesgo, puedes informarnos sobre lo ocurrido.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { navController.navigate("categorias") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        text = "Hacer Reporte",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Mis Reportes",
                    style = MaterialTheme.typography.displaySmall,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Puedes ver y actualizar tus reportes en cualquier momento.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isAuthenticated) {
                            navController.navigate("mis_reportes")
                        } else {
                            showingDialogFor = "reports"
                            showLoginDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        text = "Ver mis reportes",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (isAuthenticated) {
                                navController.navigate("mapa_general")
                            } else {
                                showingDialogFor = "map"
                                showLoginDialog = true
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(48.dp),
                        containerColor = Primary,
                        contentColor = TextPrimary
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_map),
                            contentDescription = "Ver mapa",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}