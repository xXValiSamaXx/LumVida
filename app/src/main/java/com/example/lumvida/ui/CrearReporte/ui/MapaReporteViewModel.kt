package com.example.lumvida.ui.CrearReporte.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lumvida.data.model.RecentSearch
import com.example.lumvida.data.repository.SearchHistoryRepository
import com.example.lumvida.network.model.NominatimResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapaReporteViewModel(
    private val searchHistoryRepository: SearchHistoryRepository
) : ViewModel() {
    val recentSearches = searchHistoryRepository.recentSearches
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
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

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

    // Añade esta clase Factory dentro de MapViewModel
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