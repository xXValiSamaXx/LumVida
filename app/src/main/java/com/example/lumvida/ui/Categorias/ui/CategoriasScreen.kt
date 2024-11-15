package com.example.lumvida.ui.Categorias.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.example.lumvida.R
import com.example.lumvida.ui.theme.*
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun OptionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(160.dp)
            .height(140.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDarkTheme) PrimaryDark.copy(alpha = 0.4f) else TextPrimary.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDarkTheme) TextPrimary else Primary,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDarkTheme) TextPrimary else Primary,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CategoriasScreen(
    navController: NavController,
    viewModel: CategoriasViewModel
) {
    val isDarkTheme = isSystemInDarkTheme()
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = if (isDarkTheme) PrimaryDark else TextPrimary,
            darkIcons = !isDarkTheme
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(if (isDarkTheme) R.drawable.fondo_rojo else R.drawable.fondo_blanco)
                    .build()
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection(navController, viewModel, isDarkTheme)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(48.dp),
                    modifier = Modifier.offset(y = (-40).dp)
                ) {
                    Text(
                        text = "¿Qué deseas reportar?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isDarkTheme) TextPrimary else Primary,
                        textAlign = TextAlign.Center
                    )

                    CategoriaGrid(navController, viewModel, isDarkTheme)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CategoriaGrid(
    navController: NavController,
    viewModel: CategoriasViewModel,
    isDarkTheme: Boolean
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(40.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(viewModel.categorias.size) { index ->
            val categoria = viewModel.categorias[index]
            OptionButton(
                text = categoria.titulo,
                icon = categoria.icono,
                onClick = {
                    viewModel.navigateToCrearReporte(navController, categoria)
                },
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HeaderSection(
    navController: NavController,
    viewModel: CategoriasViewModel,
    isDarkTheme: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isDarkTheme) TextPrimary else Primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .statusBarsPadding()
        ) {
            Text(
                text = "La Ciudad hoy",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = if (isDarkTheme) Primary else TextPrimary
            )

            Text(
                text = viewModel.formattedDate,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = if (isDarkTheme) Primary else TextPrimary
            )
        }
    }
}