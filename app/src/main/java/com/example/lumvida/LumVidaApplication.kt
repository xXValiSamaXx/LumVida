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