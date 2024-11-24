package com.example.lumvida.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun NetworkStatusMessage(
    isConnected: Boolean,
    wasDisconnected: Boolean, // Nuevo parámetro
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    // Solo mostrar el mensaje si estaba desconectado y ahora está conectado
    LaunchedEffect(isConnected, wasDisconnected) {
        isVisible = if (wasDisconnected && isConnected) {
            true // Mostrar mensaje solo cuando se recupera la conexión
        } else if (!isConnected) {
            true // Mostrar mensaje cuando se pierde la conexión
        } else {
            false // No mostrar mensaje en otros casos
        }

        if (isVisible) {
            delay(3000)
            isVisible = false
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFE53935),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.SignalWifi4Bar else Icons.Default.SignalWifiOff,
                    contentDescription = null,
                    tint = Color.White
                )
                Text(
                    text = if (isConnected) "Se restauró la conexión a internet"
                    else "En este momento no tienes conexión",
                    color = Color.White
                )
            }
        }
    }
}