package com.example.lumviva.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.example.lumviva.R

@Composable
fun BackgroundContainer(
    isDarkTheme: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) PrimaryDark else Color.White)
    ) {
        // Si quieres mantener la imagen pero más visible, ajusta el alpha a un valor mayor
        val context = LocalContext.current
        val imageRes = if (isDarkTheme) R.drawable.fondo_rojo else R.drawable.fondo_blanco

        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context)
                .data(imageRes)
                .size(coil.size.Size.ORIGINAL)
                .scale(Scale.FIT)
                .build()
        )

        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.5f  // Aumentado de 0.1f a 0.3f para mayor visibilidad
        )

        content()
    }
}