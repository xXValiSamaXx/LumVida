package com.example.lumvida.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert
    suspend fun insertSearch(search: RecentSearchEntity)

    @Delete
    suspend fun deleteSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches")
    suspend fun clearAllSearches()

    // Añadir esta consulta
    @Query("DELETE FROM recent_searches WHERE name = :name AND address = :address AND timestamp = :timestamp")
    suspend fun deleteSearchByDetails(name: String, address: String, timestamp: Long)
}