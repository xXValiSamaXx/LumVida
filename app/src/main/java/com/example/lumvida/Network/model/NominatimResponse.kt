package com.example.lumvida.network.model

import com.google.gson.annotations.SerializedName

data class NominatimResponse(
    @SerializedName("place_id")
    val placeId: Long,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lon")
    val lon: String,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("importance")
    val importance: Double
)