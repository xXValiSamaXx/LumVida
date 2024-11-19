package com.example.lumvida.ui.CrearReporte.ui

import androidx.core.content.ContextCompat
import com.example.lumvida.R
import com.example.lumvida.network.model.NominatimResponse
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

fun List<CrearReporteViewModel.ReporteMap>.calculateCenter(): GeoPoint {
    val latSum = sumOf { it.latitud }
    val lonSum = sumOf { it.longitud }
    return GeoPoint(latSum / size, lonSum / size)
}

fun List<GeoPoint>.calculateCircleRadius(): Double {
    if (isEmpty()) return 0.0

    // Radio mínimo de 100 metros para áreas urbanas sin ser demasiado grande ni demasiado pequeño
    val MINIMUM_RADIUS = 50.0

    val center = GeoPoint(
        map { it.latitude }.average(),
        map { it.longitude }.average()
    )

    var maxDistance = 0.0
    forEach { point ->
        val distance = calculateDistance(center, point)
        if (distance > maxDistance) maxDistance = distance
    }

    return maxOf(maxDistance * 1.2, MINIMUM_RADIUS)
}

private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
    val earthRadius = 6371000.0
    val lat1 = Math.toRadians(point1.latitude)
    val lat2 = Math.toRadians(point2.latitude)
    val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
    val deltaLon = Math.toRadians(point2.longitude - point1.longitude)

    val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
            Math.cos(lat1) * Math.cos(lat2) *
            Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2)

    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return earthRadius * c
}

fun GeoPoint.createCirclePoints(radiusMeters: Double): ArrayList<GeoPoint> {
    val points = ArrayList<GeoPoint>()
    val earthRadius = 6378137.0
    val lat = Math.toRadians(latitude)
    val lon = Math.toRadians(longitude)
    val d = radiusMeters / earthRadius

    for (i in 0..360) {
        val bearing = Math.toRadians(i.toDouble())
        val latPoint = Math.asin(
            Math.sin(lat) * Math.cos(d) + Math.cos(lat) * Math.sin(d) * Math.cos(bearing)
        )
        val lonPoint = lon + Math.atan2(
            Math.sin(bearing) * Math.sin(d) * Math.cos(lat),
            Math.cos(d) - Math.sin(lat) * Math.sin(latPoint)
        )
        points.add(GeoPoint(Math.toDegrees(latPoint), Math.toDegrees(lonPoint)))
    }
    return points
}

fun createSimpleStreetOverlay(mapView: org.osmdroid.views.MapView, result: NominatimResponse) {
    val marker = Marker(mapView).apply {
        id = "search_overlay"
        position = GeoPoint(result.lat.toDouble(), result.lon.toDouble())
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        icon = ContextCompat.getDrawable(mapView.context, R.drawable.ic_marker_default)?.apply {
            setTint(android.graphics.Color.rgb(255, 0, 255))
        }
        title = result.displayName
    }
    mapView.overlays.add(marker)
    mapView.invalidate()
}

fun createNeighborhoodCircle(center: GeoPoint): ArrayList<GeoPoint> {
    val points = ArrayList<GeoPoint>()
    val radius = 0.003 // Aproximadamente 300 metros

    for (i in 0..360 step 10) {
        val radian = Math.toRadians(i.toDouble())
        val lat = center.latitude + radius * Math.cos(radian)
        val lon = center.longitude + radius * Math.sin(radian)
        points.add(GeoPoint(lat, lon))
    }

    return points
}