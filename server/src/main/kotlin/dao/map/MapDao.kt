package com.pioneer.dao.map

import com.pioneer.domain.map.BoundsDto
import com.pioneer.domain.map.LocationDto
import com.pioneer.domain.map.LocationSearchDto
import com.pioneer.domain.map.LocationUpdateRequestDto

interface MapDao {
    suspend fun findInBounds(bounds: BoundsDto, zoom: Int?, limit: Int): List<LocationDto>

    suspend fun findById(id: String): LocationDto?

    suspend fun search(search: LocationSearchDto, limit: Int): List<LocationDto>

    suspend fun create(location: LocationDto): LocationDto

    suspend fun update(id: String, location: LocationUpdateRequestDto): LocationDto?

    suspend fun delete(id: String): Boolean
}
