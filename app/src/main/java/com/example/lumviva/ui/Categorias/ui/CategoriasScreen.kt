package com.example.lumviva.ui.Categorias.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun OptionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(110.dp)
            .height(100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
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
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 16.sp
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
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8B0000))
    ) {
        Header(navController, viewModel) // Pasando el viewModel al Header

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                Text(
                    text = "¿Qué deseas reportar?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                CategoriaGrid(navController, viewModel)
            }
        }
    }
}

@Composable
private fun CategoriaGrid(
    navController: NavController,
    viewModel: CategoriasViewModel
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.SpaceBetween,
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
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun Header(
    navController: NavController,
    viewModel: CategoriasViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 16.dp)
            .statusBarsPadding()
    ) {
        Text(
            text = "La Ciudad hoy",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            color = Color.Black
        )

        Text(
            text = viewModel.formattedDate,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}