package com.example.lumviva.ui.Inicio.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun InicioScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Espacio para el logo de Quintana Roo
        Box(modifier = Modifier.size(100.dp))

        // Espacio para el logo de LumViva
        Box(modifier = Modifier.size(100.dp))

        Text(
            text = "LumViva",
            style = MaterialTheme.typography.headlineMedium
        )

        // Espacio para el icono de la app
        Box(modifier = Modifier.size(100.dp))

        Text(
            text = "LumViva es una app que permite reportar problemas urbanos como baches o fallas en el alumbrado, directamente desde tu móvil. Conecta a los ciudadanos con las autoridades para soluciones rápidas y eficaces. ¡Haz tu ciudad mejor con LumViva!",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Button(
            onClick = { navController.navigate("reportes") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hacer reportes")
        }
    }
}