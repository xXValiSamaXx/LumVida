/*La pantalla maneja la presentación inicial de la aplicación, incluyendo un
diálogo de términos y condiciones y un diálogo de permisos. Cuando se inicia
la aplicación, primero muestra el logotipo y una descripción, y luego solicita
 al usuario que acepte los términos y conceda permisos de ubicación y cámara antes
  de permitirle continuar. */

package com.example.lumvida.ui.Inicio.ui

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.lumvida.R
import com.example.lumvida.utils.TermsAndConditions
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

    val showPermissionsDialog = remember { mutableStateOf(true) }
    var showTermsDialog by remember { mutableStateOf(true) }
    var termsAccepted by remember { mutableStateOf(false) }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Memorizar recursos
    val logoImageRes = remember(isDarkTheme) {
        if (isDarkTheme) R.drawable.logo_blanco else R.drawable.logo_rojo
    }

    val imageRequest = remember(logoImageRes) {
        ImageRequest.Builder(context)
            .data(logoImageRes)
            .crossfade(true)
            .build()
    }

    // Contenedor principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(
                id = if (isDarkTheme) R.drawable.fondo_rojo else R.drawable.fondo_blanco
            ),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        // Contenido principal
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
                enabled = termsAccepted,
                onShowDialog = { showPermissionsDialog.value = true },
                onNavigate = {
                    navController.navigate("reportes") {
                        popUpTo("inicio") { inclusive = true }
                    }
                }
            )
        }

        // Diálogos y overlays
        if (showTermsDialog) {
            TermsDialog(
                isDarkTheme = isDarkTheme,
                onAccept = {
                    termsAccepted = true
                    showTermsDialog = false
                },
                onReject = {
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
            )
        }

        if (showPermissionsDialog.value && !permissionsState.allPermissionsGranted && termsAccepted) {
            PermissionsDialog(
                onConfirm = {
                    permissionsState.launchMultiplePermissionRequest()
                    showPermissionsDialog.value = false
                },
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
private fun TermsDialog(
    isDarkTheme: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Dialog(onDismissRequest = { }) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isDarkTheme) PrimaryDark else Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Términos y Condiciones de Uso de LumVida",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .weight(1f, false)
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = TermsAndConditions.TERMS_AND_CONDITIONS,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDarkTheme) TextPrimary else PrimaryDark,
                        textAlign = TextAlign.Justify
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onReject) {
                        Text("Rechazar", color = Color.Red)
                    }
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionsDialog(
    onConfirm: () -> Unit,
    isDarkTheme: Boolean
) {
    Dialog(onDismissRequest = { }) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isDarkTheme) PrimaryDark else Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Permisos necesarios",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.permissions_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDarkTheme) TextPrimary else PrimaryDark
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Conceder permisos")
                }
            }
        }
    }
}

@Composable
private fun ContinueButton(
    permissionsGranted: Boolean,
    enabled: Boolean,
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
            contentColor = TextPrimary,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        enabled = enabled
    ) {
        Text(
            text = if (permissionsGranted) "Continuar" else "Conceder permisos",
            style = MaterialTheme.typography.labelLarge
        )
    }
}