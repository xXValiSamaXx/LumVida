package com.example.lumvida.ui.CrearReporte.ui

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import coil.load
import coil.request.CachePolicy
import coil.transform.RoundedCornersTransformation
import com.example.lumvida.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.text.SimpleDateFormat
import java.util.Locale

class CustomInfoWindow(
    mapView: MapView
) : InfoWindow(R.layout.marker_info_window, mapView) {

    override fun onOpen(item: Any?) {
        try {
            val marker = item as? Marker ?: return
            val reporte = marker.relatedObject as? CrearReporteViewModel.ReporteMap ?: return

            // Añadir listener para cerrar al tocar
            view.setOnClickListener {
                close()
            }

            // Configurar y cargar la imagen
            view.findViewById<ImageView>(R.id.markerImage)?.apply {
                load(reporte.foto) {
                    crossfade(true)
                    crossfade(300)
                    placeholder(R.drawable.iconocamara)
                    error(R.drawable.ic_marker_default)
                    memoryCachePolicy(CachePolicy.ENABLED)
                    diskCachePolicy(CachePolicy.ENABLED)
                    transformations(RoundedCornersTransformation(8f))
                    listener(
                        onStart = {
                            Log.d("CustomInfoWindow", "Iniciando carga de imagen")
                        },
                        onSuccess = { _, _ ->
                            Log.d("CustomInfoWindow", "Imagen cargada exitosamente")
                            this@CustomInfoWindow.view.invalidate()
                        },
                        onError = { _, result ->
                            Log.e("CustomInfoWindow", "Error al cargar imagen: ${result.throwable.message}")
                        }
                    )
                }
            }

            // Establecer los textos
            view.findViewById<TextView>(R.id.tvCategoria)?.text = "🏷️ Categoría: ${reporte.categoria}"
            view.findViewById<TextView>(R.id.tvDireccion)?.text = "📍 Dirección: ${reporte.direccion}"
            view.findViewById<TextView>(R.id.tvComentario)?.text = "💬 Comentario: ${reporte.comentario}"
            view.findViewById<TextView>(R.id.tvFecha)?.text = "📅 Fecha: ${formatearFecha(reporte.fecha)}"
            view.findViewById<TextView>(R.id.tvEstado)?.text = "📊 Estado: ${reporte.estado}"

        } catch (e: Exception) {
            Log.e("CustomInfoWindow", "Error al abrir InfoWindow", e)
        }
    }

    override fun onClose() {
        try {
            // Limpiar la imagen y el listener
            view.setOnClickListener(null)
            view.findViewById<ImageView>(R.id.markerImage)?.setImageDrawable(null)
        } catch (e: Exception) {
            Log.e("CustomInfoWindow", "Error al cerrar InfoWindow", e)
        }
    }

    private fun formatearFecha(timestamp: com.google.firebase.Timestamp): String {
        return try {
            val fecha = timestamp.toDate()
            val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "MX"))
            formato.format(fecha)
        } catch (e: Exception) {
            Log.e("CustomInfoWindow", "Error al formatear fecha", e)
            "Fecha no disponible"
        }
    }
}