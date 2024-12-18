package com.example.lumvida.network.model

import com.google.gson.annotations.SerializedName
import org.osmdroid.util.GeoPoint
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class OverpassResponse(
    @SerializedName("elements")
    val elements: List<OverpassElement>
)

data class OverpassElement(
    @SerializedName("type")
    val type: String,
    @SerializedName("id")
    val id: Long,
    @SerializedName("nodes")
    val nodes: List<Long>? = null,
    @SerializedName("lat")
    val lat: Double? = null,
    @SerializedName("lon")
    val lon: Double? = null,
    @SerializedName("tags")
    val tags: Map<String, String>? = null,
    @SerializedName("geometry")
    val geometry: List<OverpassGeometry>? = null
)

data class OverpassGeometry(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double
)


/*define una función buildStreetQuery que genera una consulta para la API Overpass.
Recibe latitud, longitud y el nombre de una calle, buscando caminos cercanos en un radio de 300 metros.*/

class OverpassService {
    companion object {
        private const val TIMEOUT = 30L

        fun buildStreetQuery(lat: Double, lon: Double, streetName: String): String {
            // Radio de búsqueda en metros (300 metros)
            val radius = 300

            return """
                [out:json][timeout:$TIMEOUT];
                way["highway"]["name"~"${streetName.replace("'", "\\'")}", i]
                    (around:$radius,$lat,$lon);
                (._;>;);
                out body geom;
            """.trimIndent()
        }

        /*La función filtra los elementos de tipo "way" en la respuesta, extrae sus coordenadas
        y las convierte en objetos GeoPoint, devolviendo una lista con esas coordenadas.
         Si no hay coordenadas, se omite ese "way".*/

        fun parseGeometryFromResponse(response: OverpassResponse): List<GeoPoint> {
            return response.elements
                .filter { it.type == "way" }
                .flatMap { way ->
                    way.geometry?.map { geom ->
                        GeoPoint(geom.lat, geom.lon)
                    } ?: emptyList()
                }
        }
    }
}