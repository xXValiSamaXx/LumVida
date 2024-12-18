package com.example.lumvida.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Definición de la interfaz RecentSearchDao, que será utilizada por Room para interactuar con la base de datos.
@Dao
interface RecentSearchDao {

    // Consulta para obtener las últimas 10 búsquedas ordenadas por timestamp de forma descendente.
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>

    // Función suspendida para insertar una búsqueda en la tabla de búsquedas recientes.
    @Insert
    suspend fun insertSearch(search: RecentSearchEntity)

    // Función suspendida para eliminar una búsqueda específica de la tabla.
    // Se utiliza el objeto RecentSearchEntity para identificar qué fila eliminar.
    @Delete
    suspend fun deleteSearch(search: RecentSearchEntity)

    // Función suspendida para eliminar todas las búsquedas de la tabla.
    @Query("DELETE FROM recent_searches")
    suspend fun clearAllSearches()

    // Eliminar una búsqueda específica de la tabla utilizando el nombre, la dirección y el timestamp.
    @Query("DELETE FROM recent_searches WHERE name = :name AND address = :address AND timestamp = :timestamp")
    suspend fun deleteSearchByDetails(name: String, address: String, timestamp: Long)
}
