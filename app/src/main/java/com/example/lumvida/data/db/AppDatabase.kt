package com.example.lumvida.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room

// Se define la clase AppDatabase, que es una base de datos utilizando Room.
// Se especifica que contiene una entidad llamada RecentSearchEntity y que la versión de la base de datos es 1.
@Database(entities = [RecentSearchEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    // Se define un método abstracto para acceder a los métodos de la tabla de la entidad RecentSearch.
    abstract fun recentSearchDao(): RecentSearchDao

    // Se define un objeto companion que ayuda a crear una instancia de la base de datos de manera segura.
    companion object {

        // El modificador @Volatile asegura que el valor de INSTANCE sea visible inmediatamente entre hilos.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Método que obtiene una instancia de la base de datos de forma segura y eficiente utilizando el patrón Singleton.
        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia ya existe, la devuelve.
            return INSTANCE ?: synchronized(this) {
                // Si la instancia no existe, se crea una nueva.
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Se pasa el contexto de la aplicación.
                    AppDatabase::class.java,    // Se especifica la clase de la base de datos.
                    "app_database"              // Se define el nombre de la base de datos.
                ).build()
                // Se asigna la nueva instancia a la variable INSTANCE para que no se cree otra vez.
                INSTANCE = instance
                // Se devuelve la instancia recién creada.
                instance
            }
        }
    }
}
