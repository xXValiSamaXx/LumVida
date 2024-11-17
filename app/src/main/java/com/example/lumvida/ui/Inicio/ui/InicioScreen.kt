package com.example.lumvida.ui.Inicio.ui

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
import com.example.lumvida.R
import com.example.lumvida.ui.theme.BackgroundContainer
import com.example.lumvida.ui.theme.Primary
import com.example.lumvida.ui.theme.PrimaryDark
import com.example.lumvida.ui.theme.TextPrimary

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
            verticalArrangement = Arrangement.Center // Changed to Center to position logo in middle
        ) {
            // Logo centered in the screen
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

            // Spacer to push content apart
            Spacer(modifier = Modifier.height(32.dp))

            // Description text
            Text(
                text = "LumVida es una app que permite reportar problemas urbanos como baches, basura acumulada, drenajes obstruidos o fallas en el alumbrado público, directamente desde tu móvil. Conecta a los ciudadanos con las autoridades para soluciones rápidas y eficaces. ¡Haz tu ciudad mejor con LumVida!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp),
                color = if (isDarkTheme) TextPrimary else PrimaryDark
            )

            // Button at the bottom
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