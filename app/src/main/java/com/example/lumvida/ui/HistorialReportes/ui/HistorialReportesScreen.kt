package com.example.lumvida.ui.HistorialReportes.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lumvida.ui.Auth.ui.AuthViewModel
import com.example.lumvida.ui.theme.BackgroundContainer
import com.example.lumvida.ui.theme.Primary
import com.example.lumvida.ui.theme.PrimaryDark
import com.example.lumvida.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialReportesScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    viewModel: HistorialReportesViewModel = viewModel(
        factory = HistorialReportesViewModel.Factory(authViewModel)
    )
) {
    val reportes by viewModel.reportes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()

    BackgroundContainer(isDarkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .statusBarsPadding()
        ) {
            // Título centrado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Mis Reportes",
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (isDarkTheme) TextPrimary else Primary,
                    textAlign = TextAlign.Center
                )
            }

            // Encabezados de la tabla
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (isDarkTheme) PrimaryDark.copy(alpha = 0.7f) else TextPrimary.copy(alpha = 0.9f),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Folio",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        color = if (isDarkTheme) TextPrimary else Primary
                    )
                    Text(
                        text = "Dirección",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(2f),
                        color = if (isDarkTheme) TextPrimary else Primary
                    )
                    Text(
                        text = "Fecha",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        color = if (isDarkTheme) TextPrimary else Primary
                    )
                    Text(
                        text = "Estado",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        color = if (isDarkTheme) TextPrimary else Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = if (isDarkTheme) TextPrimary else Primary)
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else if (reportes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes reportes registrados",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = if (isDarkTheme) TextPrimary else Primary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(reportes) { reporte ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isDarkTheme)
                                PrimaryDark.copy(alpha = 0.5f)
                            else
                                TextPrimary.copy(alpha = 0.8f),
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = reporte.folio.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f),
                                        color = if (isDarkTheme) TextPrimary else Primary
                                    )
                                    Text(
                                        text = reporte.direccion,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(2f),
                                        color = if (isDarkTheme) TextPrimary else Primary
                                    )
                                    Text(
                                        text = formatDate(reporte.fecha),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f),
                                        color = if (isDarkTheme) TextPrimary else Primary
                                    )
                                    Text(
                                        text = reporte.estado,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f),
                                        color = if (isDarkTheme) TextPrimary else Primary
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))
    return formatter.format(date)
}