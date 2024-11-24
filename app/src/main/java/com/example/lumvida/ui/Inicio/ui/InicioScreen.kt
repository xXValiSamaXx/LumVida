package com.example.lumvida.ui.Inicio.ui

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.lumvida.R
import com.example.lumvida.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun InicioScreen(
    navController: NavController,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    // Estado para los permisos múltiples
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Estado para controlar si mostrar el diálogo de permisos
    var showPermissionsDialog by remember { mutableStateOf(true) }

    BackgroundContainer(isDarkTheme = isDarkTheme) {
        // Diálogo de permisos
        if (showPermissionsDialog && !permissionsState.allPermissionsGranted) {
            AlertDialog(
                onDismissRequest = { /* El usuario no puede cerrar el diálogo tocando fuera */ },
                title = {
                    Text(
                        "Permisos necesarios",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                },
                text = {
                    Text(
                        text = "Para usar LumVida necesitamos:\n\n" +
                                "• Cámara: Para tomar fotos de los problemas urbanos\n" +
                                "• Ubicación: Para identificar dónde ocurren los problemas\n\n" +
                                "Estos permisos son necesarios para el funcionamiento correcto de la aplicación.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isDarkTheme) TextPrimary else PrimaryDark
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            permissionsState.launchMultiplePermissionRequest()
                            showPermissionsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("Conceder permisos")
                    }
                },
                containerColor = if (isDarkTheme) PrimaryDark else Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            val logoImageRes = if (isDarkTheme) R.drawable.logo_blanco else R.drawable.logo_rojo
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(logoImageRes)
                        .build()
                ),
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(vertical = 32.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "LumVida es una app que permite reportar problemas urbanos como baches, basura acumulada, drenajes obstruidos o fallas en el alumbrado público, directamente desde tu móvil. Conecta a los ciudadanos con las autoridades para soluciones rápidas y eficaces. ¡Haz tu ciudad mejor con LumVida!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp),
                color = if (isDarkTheme) TextPrimary else PrimaryDark
            )

            Button(
                onClick = {
                    if (!permissionsState.allPermissionsGranted) {
                        showPermissionsDialog = true
                    } else {
                        navController.navigate("reportes") {
                            popUpTo("inicio") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    text = if (permissionsState.allPermissionsGranted) "Continuar" else "Conceder permisos",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}