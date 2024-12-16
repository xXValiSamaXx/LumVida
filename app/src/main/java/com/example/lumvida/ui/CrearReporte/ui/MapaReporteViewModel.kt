/*gestiona el historial de búsquedas de ubicaciones. Sus principales funcionalidades son:

Mantiene un flujo de búsquedas recientes utilizando un repositorio de historial de búsquedas.
Permite guardar nuevas ubicaciones seleccionadas en el historial de búsquedas.
Proporciona métodos para agregar y eliminar búsquedas recientes.
Gestiona un estado de ubicación seleccionada con detalles como latitud, longitud y dirección.
Utiliza corrutinas de Kotlin para operaciones asincrónicas.
Incluye un Factory personalizado para la creación del ViewModel, que es un patrón común en la arquitectura
 MVVM (Model-View-ViewModel) de Android.

El ViewModel actúa como un intermediario entre la interfaz de usuario (el mapa) y el repositorio de
 datos, gestionando la lógica de almacenamiento y recuperación de búsquedas de ubicaciones.*/

package com.example.lumvida.ui.CrearReporte.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumvida.data.model.RecentSearch
import com.example.lumvida.data.repository.SearchHistoryRepository
import com.example.lumvida.network.model.NominatimResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.osmdroid.api.IGeoPoint

class MapaReporteViewModel(
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {

    private val _recentSearches = MutableStateFlow<List<RecentSearch>>(emptyList())
    val recentSearches = _recentSearches.asStateFlow()

    // Nuevo estado para la ubicación seleccionada
    private val _selectedLocation = MutableStateFlow<LocationState?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()

    init {
        viewModelScope.launch {
            searchHistoryRepository.recentSearches
                .map { entities ->
                    entities.map { entity ->
                        RecentSearch(
                            name = entity.name,
                            address = entity.address,
                            timestamp = entity.timestamp,
                            lat = entity.latitude,
                            lon = entity.longitude
                        )
                    }
                }
                .collect {
                    _recentSearches.value = it
                }
        }
    }

    fun onLocationSelected(position: IGeoPoint, address: String) {
        viewModelScope.launch {
            // Guardar en búsquedas recientes
            searchHistoryRepository.addSearch(
                name = address.split(",").first(),
                address = address,
                lat = position.latitude,
                lon = position.longitude
            )

            // Actualizar el estado de ubicación seleccionada
            _selectedLocation.value = LocationState(
                latitude = position.latitude,
                longitude = position.longitude,
                address = address
            )
        }
    }

    fun addToRecentSearches(suggestion: NominatimResponse) {
        viewModelScope.launch {
            searchHistoryRepository.addSearch(
                name = suggestion.displayName.split(",").first(),
                address = suggestion.displayName,
                lat = suggestion.lat.toDouble(),
                lon = suggestion.lon.toDouble()
            )
        }
    }

    fun removeFromRecentSearches(search: RecentSearch) {
        viewModelScope.launch {
            searchHistoryRepository.removeSearch(search)
        }
    }

    companion object {
        class Factory(
            private val searchHistoryRepository: SearchHistoryRepository
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MapaReporteViewModel::class.java)) {
                    return MapaReporteViewModel(searchHistoryRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

// Data class para mantener el estado de la ubicación seleccionada
data class LocationState(
    val latitude: Double,
    val longitude: Double,
    val address: String
)