/*establece límites geográficos del estado (latitud y longitud mínima y máxima),
 su centro geográfico, diferentes niveles de zoom para mapas (desde un zoom amplio
 para ver todo el estado hasta un zoom muy cercano), y un radio de círculo de aproximadamente 15 metros.*/

package com.example.lumvida.ui.CrearReporte.ui

object MapConstants {
    // Constantes para los límites de Quintana Roo
    const val QUINTANA_ROO_LAT_MIN = 18.0
    const val QUINTANA_ROO_LAT_MAX = 21.6
    const val QUINTANA_ROO_LON_MIN = -89.5
    const val QUINTANA_ROO_LON_MAX = -86.7
    const val QUINTANA_ROO_CENTER_LAT = 20.5001889
    const val QUINTANA_ROO_CENTER_LON = -87.796146

    // Constantes para los niveles de zoom
    const val ZOOM_LEVEL_1 = 14.0  // Azul
    const val ZOOM_LEVEL_2 = 16.0  // Verde
    const val ZOOM_LEVEL_3 = 18.0  // Amarillo
    const val ZOOM_LEVEL_4 = 22.0  // Rojo
    const val ZOOM_LEVEL_STATE = 8.0 // Zoom para ver todo el estado
    const val ZOOM_LEVEL_LOCAL = 15.0 // Zoom para ubicación local
    const val CIRCLE_RADIUS = 0.00015 // Aproximadamente 15 metros

    // Constantes para los límites del mapa
    const val MIN_ZOOM = ZOOM_LEVEL_1
    const val MAX_ZOOM = ZOOM_LEVEL_4
}