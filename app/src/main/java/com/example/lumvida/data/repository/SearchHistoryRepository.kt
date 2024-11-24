package com.example.lumvida.data.repository

import com.example.lumvida.data.db.RecentSearchDao
import com.example.lumvida.data.db.RecentSearchEntity
import com.example.lumvida.data.model.RecentSearch
import kotlinx.coroutines.flow.firstOrNull

class SearchHistoryRepository(private val recentSearchDao: RecentSearchDao) {
    val recentSearches = recentSearchDao.getRecentSearches()

    suspend fun addSearch(name: String, address: String, lat: Double, lon: Double) {
        recentSearchDao.insertSearch(
            RecentSearchEntity(
                name = name,
                address = address,
                timestamp = System.currentTimeMillis(),
                latitude = lat,
                longitude = lon
            )
        )
    }

    suspend fun removeSearch(search: RecentSearch) {
        // Buscar la entidad que coincida con el modelo RecentSearch
        recentSearches.firstOrNull()?.find { entity ->
            entity.name == search.name &&
                    entity.address == search.address &&
                    entity.timestamp == search.timestamp
        }?.let { entityToDelete ->
            recentSearchDao.deleteSearch(entityToDelete)
        }
    }

    suspend fun clearHistory() {
        recentSearchDao.clearAllSearches()
    }
}