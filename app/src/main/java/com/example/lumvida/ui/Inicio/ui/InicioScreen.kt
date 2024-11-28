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
import androidx.compose.ui.res.stringResource
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
    val context = LocalContext.current

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val showPermissionsDialog = remember { mutableStateOf(true) }

    // Memorizar recursos
    val logoImageRes = remember(isDarkTheme) {
        if (isDarkTheme) R.drawable.logo_blanco else R.drawable.logo_rojo
    }

    // Memorizar ImageRequest con el contexto correcto
    val imageRequest = remember(logoImageRes) {
        ImageRequest.Builder(context)
            .data(logoImageRes)
            .crossfade(true)
            .build()
    }

    BackgroundContainer(isDarkTheme = isDarkTheme) {
        if (showPermissionsDialog.value && !permissionsState.allPermissionsGranted) {
            PermissionsDialog(
                onConfirm = {
                    permissionsState.launchMultiplePermissionRequest()
                    showPermissionsDialog.value = false
                },
                isDarkTheme = isDarkTheme
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageRequest,
                    contentScale = ContentScale.Fit
                ),
                contentDescription = "Logo",
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(vertical = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.app_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp),
                color = if (isDarkTheme) TextPrimary else PrimaryDark
            )

            ContinueButton(
                permissionsGranted = permissionsState.allPermissionsGranted,
                onShowDialog = { showPermissionsDialog.value = true },
                onNavigate = {
                    navController.navigate("reportes") {
                        popUpTo("inicio") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun PermissionsDialog(
    onConfirm: () -> Unit,
    isDarkTheme: Boolean
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                "Permisos necesarios",
                style = MaterialTheme.typography.titleLarge,
                color = if (isDarkTheme) TextPrimary else PrimaryDark
            )
        },
        text = {
            Text(
                text = stringResource(R.string.permissions_description),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDarkTheme) TextPrimary else PrimaryDark
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
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

@Composable
private fun ContinueButton(
    permissionsGranted: Boolean,
    onShowDialog: () -> Unit,
    onNavigate: () -> Unit
) {
    Button(
        onClick = {
            if (!permissionsGranted) {
                onShowDialog()
            } else {
                onNavigate()
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
            text = if (permissionsGranted) "Continuar" else "Conceder permisos",
            style = MaterialTheme.typography.labelLarge
        )
    }
}