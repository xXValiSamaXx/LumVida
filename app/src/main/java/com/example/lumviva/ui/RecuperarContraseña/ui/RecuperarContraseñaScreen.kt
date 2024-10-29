package com.example.lumviva.ui.RecuperarContraseña.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lumviva.ui.RecuperarContrasena.ui.RecuperarContraseñaViewModel
import com.example.lumviva.ui.RecuperarContrasena.ui.RecuperarContraseñaState
import com.example.lumviva.ui.Auth.ui.AuthViewModel
import com.example.lumviva.ui.theme.BackgroundContainer
import com.example.lumviva.ui.theme.Primary
import com.example.lumviva.ui.theme.PrimaryDark
import com.example.lumviva.ui.theme.Secondary
import com.example.lumviva.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarContraseñaScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: RecuperarContraseñaViewModel = viewModel(
        factory = RecuperarContraseñaViewModel.Factory(authViewModel)
    ),
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    var email by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is RecuperarContraseñaState.Success) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.navigateUp()
            },
            title = { Text(
                "Correo enviado",
                style = MaterialTheme.typography.titleLarge,
                color = if (isDarkTheme) TextPrimary else PrimaryDark
            )  },
            text = {
                Text(
                    "Se han enviado las instrucciones de recuperación a tu correo electrónico. " +
                            "Por favor, revisa tu bandeja de entrada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Aceptar")
                }
            },
            containerColor = if (isDarkTheme) PrimaryDark else Color.White
        )
    }

    BackgroundContainer(isDarkTheme = isDarkTheme) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = if (isDarkTheme) TextPrimary else PrimaryDark,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Recuperar contraseña",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isDarkTheme) TextPrimary else PrimaryDark
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                "Regresar",
                                tint = if (isDarkTheme) TextPrimary else PrimaryDark
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
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
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trim() },
                    label = { Text("Correo electrónico", color = if (isDarkTheme) TextPrimary else PrimaryDark) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state !is RecuperarContraseñaState.Loading,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                        unfocusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Secondary,
                        disabledTextColor = if (isDarkTheme) TextPrimary.copy(alpha = 0.6f) else PrimaryDark.copy(alpha = 0.6f)
                    )
                )

                Button(
                    onClick = { viewModel.recuperarContraseña(email) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotEmpty() && state !is RecuperarContraseñaState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary,
                        disabledContainerColor = Primary.copy(alpha = 0.6f)
                    )
                ) {
                    if (state is RecuperarContraseñaState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = TextPrimary
                        )
                    } else {
                        Text("Enviar instrucciones")
                    }
                }

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
}