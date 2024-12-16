/*componente de Compose que se utiliza para crear un fondo dinámico según el tema
(oscuro o claro). Este componente recibe una propiedad isDarkTheme que determina
si se debe usar un fondo oscuro o claro, ajustando el color de fondo en función de
la opción. Utiliza la librería Coil para cargar y mostrar una imagen de fondo (una para el
 tema oscuro y otra para el claro) con un alpha de 0.5f, lo que hace que la imagen se vea
  parcialmente transparente y permita que el contenido encima de ella sea más visible. El
  content es una función composable que se renderiza encima de esta imagen de fondo,
  permitiendo personalizar el contenido visual de la pantalla según las necesidades.*/

package com.example.lumvida.ui.theme

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
import com.example.lumvida.R

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