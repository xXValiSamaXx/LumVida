package com.example.lumvida.network.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class InfraestructuraResponse(
    @SerializedName("nombreEquipo")
    val nombreEquipo: String,
    @SerializedName("datosTablas")
    val datosTablas: DatosTablas
)

data class DatosTablas(
    @SerializedName("comedatos_preticor")
    val comedatosPreticor: List<TipoInfraestructura>
)

data class TipoInfraestructura(
    @SerializedName("id")
    val id: Int,
    @SerializedName("tipo_infraestructura")
    val tipoInfraestructura: String,
    @SerializedName("info_requerida")
    val infoRequerida: String,
    @SerializedName("detalle_ubicacion")
    val detalleUbicacion: String,
    @SerializedName("descripcion_problema")
    val descripcionProblema: String,
    @SerializedName("datos_especificos")
    val datosEspecificos: String,
    @SerializedName("tiempo_respuesta")
    val tiempoRespuesta: String,
    @SerializedName("seguimieto")
    val seguimiento: String,
    @SerializedName("formato_recepcion")
    val formatoRecepcion: String
)