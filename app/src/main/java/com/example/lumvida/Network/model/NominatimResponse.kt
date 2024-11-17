package com.example.lumvida.Network.model

import com.google.gson.annotations.SerializedName

data class NominatimResponse(
    @SerializedName("place_id")
    val placeId: Long,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lon")
    val lon: String,
    @SerializedName("display_name")
    val display_name: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("importance")
    val importance: Double
)