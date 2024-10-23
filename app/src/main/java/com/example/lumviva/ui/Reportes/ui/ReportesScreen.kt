package com.example.lumviva.ui.Reportes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lumviva.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    reportesViewModel: ReportesViewModel = viewModel { ReportesViewModel(authViewModel) }
) {
    val userName by reportesViewModel.userName.collectAsState(initial = "")
    val isAuthenticated by reportesViewModel.isAuthenticated.collectAsState(initial = false)
    var showAuthDialog by remember { mutableStateOf(false) }
    var currentAction by remember { mutableStateOf<() -> Unit>({}) }

    // Colores personalizados
    val guindaColor = Color(0xFF9B1B30)
    val azulColor = Color(0xFF0D7CF2)
    val grisClaroColor = Color(0xFFF0F2F5)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado
            TopAppBar(title = {
                Text(
                    text = "Bienvenido, $userName",
                    modifier = Modifier
                        .fillMaxWidth() // Para ocupar el ancho completo
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .padding(top=32.dp), // Centrar horizontalmente
                        fontWeight = FontWeight.Bold // Opción para hacer el texto más destacado
                    )
            })

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Haz un reporte",
                style = MaterialTheme.typography.titleLarge.copy( // Usar titleLarge en vez de h5
                    color = Color(0xFF111418),
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Si has presenciado o experimentado un problema urbano, como alcantarillado obstruido, mal estado de las calles o" +
                        " situaciones de riesgo, puedes informarnos sobre lo ocurrido.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF111418)),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { /* Acción para hacer el reporte */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFFFC3939))
            ) {
                Text(
                    text = "Hacer Reporte",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(52.dp))

            Text(
                text = "Mis Reportes",
                style = MaterialTheme.typography.titleLarge.copy( // Usar titleLarge
                    color = Color(0xFF111418),
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Puedes ver y actualizar tus reportes en cualquier momento.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF111418)),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { /* Acción para ver mis reportes */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFFFC3939))
            ) {
                Text(
                    text = "Ver mis reportes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}