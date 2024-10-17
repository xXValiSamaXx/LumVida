package com.example.lumviva.ui.crearcuenta

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lumviva.ui.auth.AuthState
import com.example.lumviva.ui.auth.AuthViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CrearCuentaScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    crearCuentaViewModel: CrearCuentaViewModel = viewModel { CrearCuentaViewModel(authViewModel) }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    val crearCuentaState by crearCuentaViewModel.state.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val passwordFocusRequester = remember { FocusRequester() }
    val confirmarPasswordFocusRequester = remember { FocusRequester() }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> navController.navigate("reportes") {
                popUpTo("login") { inclusive = true }
            }
            is AuthState.Error -> {
                // Mostrar error de autenticación
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
                .padding(32.dp),
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
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it.filter { c -> c != ' ' } },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
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
                    .padding(bottom = 16.dp)
                    .focusRequester(passwordFocusRequester)
            )

            OutlinedTextField(
                value = confirmarPassword,
                onValueChange = { confirmarPassword = it.filter { c -> c != ' ' } },
                label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        crearCuentaViewModel.crearCuenta(email, password, confirmarPassword)
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmarPasswordFocusRequester)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { crearCuentaViewModel.crearCuenta(email, password, confirmarPassword) },
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