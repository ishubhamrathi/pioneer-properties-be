package com.pioneer.service.map

import com.pioneer.dao.map.MapDao
import com.pioneer.domain.map.LocationCreateRequestDto
import com.pioneer.domain.map.LocationDto
import com.pioneer.domain.map.LocationUpdateRequestDto
import java.util.UUID

interface LocationManagementService {
    suspend fun createLocation(location: LocationCreateRequestDto): LocationDto

    suspend fun updateLocation(id: String, location: LocationUpdateRequestDto): LocationDto?

    suspend fun deleteLocation(id: String): Boolean
}

class DefaultLocationManagementService(
    private val dao: MapDao,
) : LocationManagementService {
    override suspend fun createLocation(location: LocationCreateRequestDto): LocationDto {
        location.validate()

        return dao.create(
            LocationDto(
                id = location.id?.takeIf(String::isNotBlank) ?: UUID.randomUUID().toString(),
                name = location.name.trim(),
                category = location.category.trim(),
                lat = location.lat,
                lng = location.lng,
                description = location.description,
                image = location.image,
                address = location.address,
                rating = location.rating,
                icon = location.icon,
                color = location.color,
            ),
        )
    }

    override suspend fun updateLocation(id: String, location: LocationUpdateRequestDto): LocationDto? {
        require(id.isNotBlank()) { "Location id is required" }
        location.validate()

        return dao.update(
            id = id,
            location = location.copy(
                name = location.name.trim(),
                category = location.category.trim(),
            ),
        )
    }

    override suspend fun deleteLocation(id: String): Boolean {
        require(id.isNotBlank()) { "Location id is required" }

        return dao.delete(id)
    }

    private fun LocationCreateRequestDto.validate() {
        require(name.isNotBlank()) { "Location name is required" }
        require(category.isNotBlank()) { "Location category is required" }
        require(lat in -90.0..90.0) { "lat must be between -90 and 90" }
        require(lng in -180.0..180.0) { "lng must be between -180 and 180" }
    }

    private fun LocationUpdateRequestDto.validate() {
        require(name.isNotBlank()) { "Location name is required" }
        require(category.isNotBlank()) { "Location category is required" }
        require(lat in -90.0..90.0) { "lat must be between -90 and 90" }
        require(lng in -180.0..180.0) { "lng must be between -180 and 180" }
    }
}
