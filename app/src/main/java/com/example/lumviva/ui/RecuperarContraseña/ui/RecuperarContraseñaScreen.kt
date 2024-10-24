package com.example.lumviva.ui.RecuperarContraseña.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lumviva.ui.RecuperarContrasena.ui.RecuperarContraseñaViewModel
import com.example.lumviva.ui.RecuperarContrasena.ui.RecuperarContraseñaState
import com.example.lumviva.ui.Auth.ui.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarContraseñaScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: RecuperarContraseñaViewModel = viewModel(
        factory = RecuperarContraseñaViewModel.Factory(authViewModel)
    )
) {
    var email by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Efecto para mostrar el diálogo cuando el estado sea Success
    LaunchedEffect(state) {
        if (state is RecuperarContraseñaState.Success) {
            showSuccessDialog = true
        }
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.navigateUp()
            },
            title = { Text("Correo enviado") },
            text = {
                Text(
                    "Se han enviado las instrucciones de recuperación a tu correo electrónico. " +
                            "Por favor, revisa tu bandeja de entrada."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigateUp()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ingresa tu correo electrónico y te enviaremos las instrucciones para restablecer tu contraseña.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is RecuperarContraseñaState.Loading,
                singleLine = true
            )

            Button(
                onClick = { viewModel.recuperarContraseña(email) },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotEmpty() && state !is RecuperarContraseñaState.Loading
            ) {
                if (state is RecuperarContraseñaState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Enviar instrucciones")
                }
            }

            // Manejar errores
            LaunchedEffect(state) {
                if (state is RecuperarContraseñaState.Error) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = (state as RecuperarContraseñaState.Error).message
                        )
                    }
                }
            }
        }
    }
}