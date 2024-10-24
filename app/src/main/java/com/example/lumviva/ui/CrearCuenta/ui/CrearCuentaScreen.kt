package com.example.lumviva.ui.crearcuenta

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lumviva.ui.Auth.ui.AuthState
import com.example.lumviva.ui.Auth.ui.AuthViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CrearCuentaScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    crearCuentaViewModel: CrearCuentaViewModel = viewModel { CrearCuentaViewModel(authViewModel) }
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmarPasswordVisible by remember { mutableStateOf(false) }
    val crearCuentaState by crearCuentaViewModel.state.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmarPasswordFocusRequester = remember { FocusRequester() }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> navController.navigate("reportes") {
                popUpTo("login") { inclusive = true }
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { emailFocusRequester.requestFocus() }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.filter { c -> c != ' ' } },
                label = { Text("Correo electrónico") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .focusRequester(emailFocusRequester)
            )

            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.filter { c -> c != ' ' } },
                    label = { Text("Contraseña") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { confirmarPasswordFocusRequester.requestFocus() }
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    }
                )

                if (password.isNotEmpty()) {
                    // Indicador de fortaleza
                    val strength = calculatePasswordStrength(password)
                    val (color, label) = when {
                        strength >= 80 -> Color(0xFF4CAF50) to "Fuerte"
                        strength >= 60 -> Color(0xFFFFA726) to "Media"
                        strength > 0 -> Color(0xFFF44336) to "Débil"
                        else -> Color.Gray to "Muy débil"
                    }

                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LinearProgressIndicator(
                                progress = strength / 100f,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                color = color
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Requisitos de la contraseña
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = "La contraseña debe contener:",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            val requirements = listOf(
                                "Al menos 8 caracteres" to (password.length >= 8),
                                "Al menos una letra mayúscula" to password.any { it.isUpperCase() },
                                "Al menos una letra minúscula" to password.any { it.isLowerCase() },
                                "Al menos un número" to password.any { it.isDigit() },
                                "Al menos un carácter especial (@#\$%^&+=)" to password.any { "@#\$%^&+=".contains(it) }
                            )

                            requirements.forEach { (requirement, isMet) ->
                                val reqColor by animateColorAsState(
                                    if (isMet) Color(0xFF4CAF50) else Color.Gray,
                                    label = "requirement color"
                                )
                                Text(
                                    text = "• $requirement",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = reqColor,
                                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmarPassword,
                onValueChange = { confirmarPassword = it.filter { c -> c != ' ' } },
                label = { Text("Confirmar contraseña") },
                visualTransformation = if (confirmarPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        crearCuentaViewModel.crearCuenta(email, password, confirmarPassword, nombre)
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmarPasswordFocusRequester),
                trailingIcon = {
                    IconButton(onClick = { confirmarPasswordVisible = !confirmarPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmarPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmarPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    crearCuentaViewModel.crearCuenta(email, password, confirmarPassword, nombre)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear cuenta")
            }

            if (crearCuentaState is CrearCuentaState.Error) {
                Text(
                    text = (crearCuentaState as CrearCuentaState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (crearCuentaState is CrearCuentaState.Loading || authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿Ya tienes una cuenta?")
                TextButton(onClick = { navController.navigate("login") }) {
                    Text("Inicia sesión")
                }
            }
        }
    }
}

private fun calculatePasswordStrength(password: String): Float {
    if (password.isEmpty()) return 0f

    var score = 0f
    val requirements = listOf(
        password.length >= 8,
        password.any { it.isUpperCase() },
        password.any { it.isLowerCase() },
        password.any { it.isDigit() },
        password.any { "@#\$%^&+=".contains(it) }
    )

    // Cada requerimiento cumplido suma 20 puntos
    score += requirements.count { it } * 20f

    // Bonus por longitud extra
    if (password.length > 8) {
        score += minOf((password.length - 8) * 2.5f, 20f)
    }

    return minOf(score, 100f)
}