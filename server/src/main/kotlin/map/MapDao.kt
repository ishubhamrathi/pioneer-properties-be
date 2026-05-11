package com.pioneer.map

interface MapDao {
    suspend fun findInBounds(bounds: BoundsDto, zoom: Int?, limit: Int): List<LocationDto>

    suspend fun findById(id: String): LocationDto?

    suspend fun search(search: LocationSearchDto, limit: Int): List<LocationDto>

    suspend fun create(location: LocationDto): LocationDto

    suspend fun update(id: String, location: LocationUpdateRequestDto): LocationDto?

    suspend fun delete(id: String): Boolean
}
