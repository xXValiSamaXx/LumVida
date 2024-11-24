package com.example.lumvida

import android.app.Application
import com.example.lumvida.data.db.AppDatabase
import com.example.lumvida.data.repository.SearchHistoryRepository

class LumVidaApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val searchHistoryRepository by lazy { SearchHistoryRepository(database.recentSearchDao()) }
}