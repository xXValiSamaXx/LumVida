/* permitir que los usuarios ingresen sus datos personales (nombre, correo, teléfono, contraseña)
 para registrarse en el sistema. Implementa validaciones de entrada, muestra un indicador de
 fortaleza de la contraseña y asegura que se cumplan requisitos específicos. También gestiona
  estados como carga, errores y autenticación, redirigiendo a otras pantallas según sea necesario.
   La interfaz está optimizada tanto para temas claros como oscuros, ofreciendo una experiencia visual
    amigable y adaptada.*/


package com.example.lumvida.ui.crearcuenta

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lumvida.ui.Auth.ui.AuthState
import com.example.lumvida.ui.Auth.ui.AuthViewModel
import com.example.lumvida.ui.theme.BackgroundContainer
import com.example.lumvida.ui.theme.Primary
import com.example.lumvida.ui.theme.PrimaryDark
import com.example.lumvida.ui.theme.Secondary
import com.example.lumvida.ui.theme.TextPrimary

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CrearCuentaScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    crearCuentaViewModel: CrearCuentaViewModel = viewModel { CrearCuentaViewModel(authViewModel) },
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmarPasswordVisible by remember { mutableStateOf(false) }
    val crearCuentaState by crearCuentaViewModel.state.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val emailFocusRequester = remember { FocusRequester() }
    val telefonoFocusRequester = remember { FocusRequester() }
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

    BackgroundContainer(isDarkTheme = isDarkTheme) {
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
                style = MaterialTheme.typography.displaySmall,
                color = if (isDarkTheme) TextPrimary else PrimaryDark,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = {
                    Text(
                        "Nombre",
                        color = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { emailFocusRequester.requestFocus() }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                    unfocusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Secondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it.filter { c -> c != ' ' } },
                label = {
                    Text(
                        "Correo electrónico",
                        color = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { telefonoFocusRequester.requestFocus() }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                    unfocusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Secondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .focusRequester(emailFocusRequester)
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = {
                    // Solo permitir números y limitar a 10 dígitos
                    if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                        telefono = it
                    }
                },
                label = {
                    Text(
                        "Teléfono",
                        color = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                    unfocusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Secondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .focusRequester(telefonoFocusRequester)
            )

            // Campo de contraseña con indicador de fortaleza
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it.filter { c -> c != ' ' } },
                    label = {
                        Text(
                            "Contraseña",
                            color = if (isDarkTheme) TextPrimary else PrimaryDark
                        )
                    },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { confirmarPasswordFocusRequester.requestFocus() }
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                        unfocusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Secondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible)
                                    "Ocultar contraseña"
                                else
                                    "Mostrar contraseña",
                                tint = if (isDarkTheme) TextPrimary else PrimaryDark
                            )
                        }
                    }
                )

                // Indicador de fortaleza y requisitos
                if (password.isNotEmpty()) {
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
                                color = color
                            )
                        }

                        // Requisitos de la contraseña
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = "La contraseña debe contener:",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDarkTheme) TextPrimary else PrimaryDark,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            val requirements = listOf(
                                "Al menos 8 caracteres" to (password.length >= 8),
                                "Al menos una letra mayúscula" to password.any { it.isUpperCase() },
                                "Al menos una letra minúscula" to password.any { it.isLowerCase() },
                                "Al menos un número" to password.any { it.isDigit() },
                                "Al menos un carácter especial (@#\$%^&+=)" to
                                        password.any { "@#\$%^&+=".contains(it) }
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
                label = {
                    Text(
                        "Confirmar contraseña",
                        color = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                },
                visualTransformation = if (confirmarPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        if (validateInputs(nombre, email, telefono, password, confirmarPassword)) {
                            crearCuentaViewModel.crearCuenta(email, password, confirmarPassword, nombre, telefono)
                        }
                    }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                    unfocusedTextColor = if (isDarkTheme) TextPrimary else PrimaryDark,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Secondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmarPasswordFocusRequester),
                trailingIcon = {
                    IconButton(onClick = { confirmarPasswordVisible = !confirmarPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmarPasswordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (confirmarPasswordVisible)
                                "Ocultar contraseña"
                            else
                                "Mostrar contraseña",
                            tint = if (isDarkTheme) TextPrimary else PrimaryDark
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (validateInputs(nombre, email, telefono, password, confirmarPassword)) {
                        crearCuentaViewModel.crearCuenta(email, password, confirmarPassword, nombre, telefono)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    "Crear cuenta",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (crearCuentaState is CrearCuentaState.Error) {
                Text(
                    text = (crearCuentaState as CrearCuentaState.Error).message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            if (crearCuentaState is CrearCuentaState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 16.dp),
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenedor para el botón "¿Ya tienes una cuenta?"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(
                    onClick = {
                        navController.navigate("login") {
                            popUpTo("crear_cuenta") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text(
                        text = "¿Ya tienes una cuenta? Inicia sesión",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                }
            }

            // Mostrar errores si existen
            if (crearCuentaState is CrearCuentaState.Error) {
                Text(
                    text = (crearCuentaState as CrearCuentaState.Error).message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Indicador de carga
            if (crearCuentaState is CrearCuentaState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 16.dp),
                    color = Primary
                )
            }
        }
    }
}

private fun validateInputs(
    nombre: String,
    email: String,
    telefono: String,
    password: String,
    confirmarPassword: String
): Boolean {
    return when {
        nombre.isBlank() -> false
        !email.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) -> false
        telefono.length != 10 -> false
        password != confirmarPassword -> false
        password.length < 8 -> false
        !password.any { it.isUpperCase() } -> false
        !password.any { it.isLowerCase() } -> false
        !password.any { it.isDigit() } -> false
        !password.any { "@#\$%^&+=".contains(it) } -> false
        else -> true
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