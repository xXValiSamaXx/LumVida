package com.example.lumviva.ui.Reportes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lumviva.ui.Auth.ui.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    reportesViewModel: ReportesViewModel = viewModel(
        factory = ReportesViewModel.Factory(authViewModel)
    )
) {
    val userName by reportesViewModel.userName.collectAsState()
    val isAuthenticated by reportesViewModel.isAuthenticated.collectAsState()
    var showLoginDialog by remember { mutableStateOf(false) }

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text("Iniciar sesión requerido") },
            text = { Text("Para ver tus reportes necesitas iniciar sesió.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLoginDialog = false
                        navController.navigate("login")
                    }
                ) {
                    Text("Iniciar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = if (isAuthenticated) "Bienvenido, $userName" else "Bienvenido Invitado",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                            .padding(top = 32.dp),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (isAuthenticated) {
                        TextButton(onClick = { reportesViewModel.logout() }) {
                            Text("Cerrar sesión")
                        }
                    } else {
                        TextButton(onClick = { navController.navigate("login") }) {
                            Text("Iniciar sesión")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Haz un reporte",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF111418),
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Si has presenciado o experimentado un problema urbano, como alcantarillado obstruido, mal estado de las calles o situaciones de riesgo, puedes informarnos sobre lo ocurrido.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF111418)),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate("categorias") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFFFC3939))
            ) {
                Text(
                    text = "Hacer Reporte",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(52.dp))

            Text(
                text = "Mis Reportes",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF111418),
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Puedes ver y actualizar tus reportes en cualquier momento.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF111418)),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isAuthenticated) {
                        navController.navigate("mis_reportes")
                    } else {
                        showLoginDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFFFC3939))
            ) {
                Text(
                    text = "Ver mis reportes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}