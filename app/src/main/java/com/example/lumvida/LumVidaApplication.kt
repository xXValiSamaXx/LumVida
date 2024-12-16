/*extiende de Application, y se encarga de inicializar ciertos componentes esenciales cuando
la aplicación se crea. En primer lugar, se configura la base de datos AppDatabase y el repositorio
 SearchHistoryRepository para gestionar el historial de búsquedas. Además, se realiza la configuración
 de OSMDroid, una librería para manejar mapas en la aplicación. Se especifica el directorio de caché
 para almacenar los archivos del mapa, se crea una caché de 100MB para los tiles (los fragmentos de los mapas),
 y se establece un tiempo de expiración de los tiles de una semana.*/

package com.example.lumvida

import android.app.Application
import com.example.lumvida.data.db.AppDatabase
import com.example.lumvida.data.repository.SearchHistoryRepository
import org.osmdroid.config.Configuration
import java.io.File

class LumVidaApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val searchHistoryRepository by lazy { SearchHistoryRepository(database.recentSearchDao()) }

    override fun onCreate() {
        super.onCreate()

        // Configurar OSMDroid
        Configuration.getInstance().apply {
            // Usar el directorio de caché de la aplicación
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")

            // Crear directorios si no existen
            osmdroidBasePath.mkdirs()
            osmdroidTileCache.mkdirs()

            // Configuraciones adicionales
            userAgentValue = packageName
            tileFileSystemCacheMaxBytes = 100L * 1024 * 1024 // 100MB máximo de caché
            expirationOverrideDuration = 1000L * 60 * 60 * 24 * 7 // 7 días de caché
        }
    }
}