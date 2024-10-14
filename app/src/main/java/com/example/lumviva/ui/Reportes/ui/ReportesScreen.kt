package com.example.lumviva.ui.Reportes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lumviva.ui.auth.AuthViewModel

@Composable
fun ReportesScreen(
    navController: NavController,
    userName: String,
    authViewModel: AuthViewModel = viewModel()
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }
    var currentAction by remember { mutableStateOf<() -> Unit>({}) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ... (código anterior para la barra superior y el logo)

            Text(
                text = "Hola, $userName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Selecciona la opción deseada",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OptionButton(
                    text = "Hacer un reporte",
                    onClick = { navController.navigate("hacer_reporte") }
                )
                OptionButton(
                    text = "Mis reportes",
                    onClick = {
                        if (isAuthenticated) {
                            navController.navigate("mis_reportes")
                        } else {
                            showAuthDialog = true
                            currentAction = { navController.navigate("mis_reportes") }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isAuthenticated) {
                        navController.navigate("mi_perfil")
                    } else {
                        showAuthDialog = true
                        currentAction = { navController.navigate("mi_perfil") }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Mi perfil")
            }
        }
    }

    if (showAuthDialog) {
        AuthDialog(
            onDismiss = { showAuthDialog = false },
            onLogin = {
                showAuthDialog = false
                navController.navigate("login")
            },
            onLater = { showAuthDialog = false }
        )
    }
}

@Composable
fun AuthDialog(onDismiss: () -> Unit, onLogin: () -> Unit, onLater: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Para usar estas características, debes iniciar sesión.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onLogin) {
                    Text("Quiero iniciar sesión")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onLater) {
                    Text("En otro momento")
                }
            }
        }
    }
}

@Composable
fun OptionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(150.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Aquí iría el ícono
            Box(modifier = Modifier.size(50.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text)
        }
    }
}