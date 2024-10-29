package com.example.lumviva.ui.Inicio.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.lumviva.R
import com.example.lumviva.ui.theme.BackgroundContainer
import com.example.lumviva.ui.theme.Primary
import com.example.lumviva.ui.theme.PrimaryDark
import com.example.lumviva.ui.theme.TextPrimary

@Composable
fun InicioScreen(
    navController: NavController,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    BackgroundContainer(isDarkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo principal en la parte superior
            val logoImageRes = if (isDarkTheme) R.drawable.logo_blanco else R.drawable.logo_rojo
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(logoImageRes)
                        .build()
                ),
                contentDescription = "Logo LumViva",
                modifier = Modifier
                    .fillMaxWidth(0.7f) // El logo ocupará el 70% del ancho
                    .padding(vertical = 32.dp),
                contentScale = ContentScale.Fit // Cambiado a Fit en lugar de FitWidth
            )

            // Resto del código permanece igual...
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "LumViva",
                style = MaterialTheme.typography.displayMedium,
                color = if (isDarkTheme) TextPrimary else PrimaryDark
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "LumViva es una app que permite reportar problemas urbanos como baches o fallas en el alumbrado, directamente desde tu móvil. Conecta a los ciudadanos con las autoridades para soluciones rápidas y eficaces. ¡Haz tu ciudad mejor con LumViva!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp),
                color = if (isDarkTheme) TextPrimary else PrimaryDark
            )

            Button(
                onClick = {
                    navController.navigate("reportes") {
                        popUpTo("inicio") { inclusive = true }
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
                    "Continuar",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}