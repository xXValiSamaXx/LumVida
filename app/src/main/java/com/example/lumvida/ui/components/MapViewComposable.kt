/*crea un mapa en la aplicación usando Jetpack Compose y osmdroid.
 La función rememberMapViewWithLifecycle maneja el ciclo de vida del mapa para
 asegurarse de que se active cuando la aplicación está en uso y se detenga cuando
 no lo está. Luego, la función MapView muestra el mapa en la interfaz y permite realizar
 acciones al crear el mapa, como configurarlo o agregar eventos.*/

package com.example.lumvida.ui.components

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.views.MapView

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            else -> {}
        }
    }

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    onMapViewCreated: (MapView) -> Unit
) {
    val mapView = rememberMapViewWithLifecycle()

    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) { view ->
        onMapViewCreated(view)
    }
}