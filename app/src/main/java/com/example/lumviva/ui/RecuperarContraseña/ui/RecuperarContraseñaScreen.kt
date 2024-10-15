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

@Composable
fun RecuperarContraseñaScreen(
    navController: NavController,
    viewModel: RecuperarContraseñaViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Barra superior con botón de regresar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar"
                    )
                }
                Text(
                    "Recuperar contraseña",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Si has olvidado tu contraseña, ingresa tu correo electrónico para que podamos enviarte instrucciones sobre cómo recuperarla.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.recuperarContraseña(email) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Recuperar contraseña")
            }
        }
    }
}