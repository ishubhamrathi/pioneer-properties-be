package com.pioneer.map

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val id: String,
    val name: String,
    val category: String,
    val lat: Double,
    val lng: Double,
    val description: String? = null,
    val image: String? = null,
    val address: String? = null,
    val rating: Double? = null,
    val icon: String? = null,
    val color: String? = null,
)

@Serializable
data class LocationsResponseDto(
    val locations: List<LocationDto>,
    val hasMore: Boolean,
)

data class BoundsDto(
    val minLat: Double,
    val minLng: Double,
    val maxLat: Double,
    val maxLng: Double,
)

data class LocationSearchDto(
    val query: String,
    val lat: Double?,
    val lng: Double?,
    val radiusKm: Double?,
)
