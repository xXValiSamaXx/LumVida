package com.example.lumvida.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// La anotación @Entity indica que esta clase se mapeará a una tabla de base de datos
// con el nombre "recent_searches". Cada instancia de esta clase representa una fila en la tabla.
@Entity(tableName = "recent_searches")


// La anotación @PrimaryKey marca este campo "id" como la clave primaria de la tabla.
// generará automáticamente un valor único para el "id".
data class RecentSearchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double
)