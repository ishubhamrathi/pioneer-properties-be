package com.pioneer.service.map

import com.pioneer.dao.map.MapDao
import com.pioneer.domain.map.BoundsDto
import com.pioneer.domain.map.LocationDto
import com.pioneer.domain.map.LocationSearchDto
import com.pioneer.domain.map.LocationsResponseDto

interface MapService {
    suspend fun getLocations(bounds: BoundsDto, zoom: Int?): LocationsResponseDto

    suspend fun getLocation(id: String): LocationDto?

    suspend fun searchLocations(search: LocationSearchDto): LocationsResponseDto
}

class DefaultMapService(
    private val dao: MapDao,
) : MapService {
    override suspend fun getLocations(bounds: BoundsDto, zoom: Int?): LocationsResponseDto {
        val rows = dao.findInBounds(bounds, zoom, PAGE_SIZE + 1)
        return rows.toResponse()
    }

    override suspend fun getLocation(id: String): LocationDto? = dao.findById(id)

    override suspend fun searchLocations(search: LocationSearchDto): LocationsResponseDto {
        val rows = dao.search(search, PAGE_SIZE + 1)
        return rows.toResponse()
    }

    private fun List<LocationDto>.toResponse(): LocationsResponseDto =
        LocationsResponseDto(
            locations = take(PAGE_SIZE),
            hasMore = size > PAGE_SIZE,
        )

    private companion object {
        const val PAGE_SIZE = 100
    }
}
