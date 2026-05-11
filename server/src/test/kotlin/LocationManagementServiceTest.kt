package com.pioneer.map

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class LocationManagementServiceTest {
    @Test
    fun `create location generates id and trims required fields`() {
        runBlocking {
            val dao = FakeMapDao()
            val service = DefaultLocationManagementService(dao)

            val location = service.createLocation(
                LocationCreateRequestDto(
                    name = "  Midtown Office  ",
                    category = "  office  ",
                    lat = 28.6139,
                    lng = 77.2090,
                ),
            )

            assertNotNull(location.id)
            assertEquals("Midtown Office", location.name)
            assertEquals("office", location.category)
            assertEquals(location, dao.locations[location.id])
        }
    }

    @Test
    fun `update location validates coordinates`() {
        runBlocking {
            val service = DefaultLocationManagementService(FakeMapDao())

            assertFailsWith<IllegalArgumentException> {
                service.updateLocation(
                    "location-1",
                    LocationUpdateRequestDto(
                        name = "Invalid",
                        category = "office",
                        lat = 91.0,
                        lng = 77.2090,
                    ),
                )
            }
        }
    }

    @Test
    fun `delete location delegates to dao`() {
        runBlocking {
            val dao = FakeMapDao()
            val service = DefaultLocationManagementService(dao)
            dao.locations["location-1"] = LocationDto(
                id = "location-1",
                name = "Midtown Office",
                category = "office",
                lat = 28.6139,
                lng = 77.2090,
            )

            assertTrue(service.deleteLocation("location-1"))
            assertTrue(dao.locations.isEmpty())
        }
    }

    private class FakeMapDao : MapDao {
        val locations = mutableMapOf<String, LocationDto>()

        override suspend fun findInBounds(bounds: BoundsDto, zoom: Int?, limit: Int): List<LocationDto> = emptyList()

        override suspend fun findById(id: String): LocationDto? = locations[id]

        override suspend fun search(search: LocationSearchDto, limit: Int): List<LocationDto> = emptyList()

        override suspend fun create(location: LocationDto): LocationDto {
            locations[location.id] = location
            return location
        }

        override suspend fun update(id: String, location: LocationUpdateRequestDto): LocationDto? {
            val updated = LocationDto(
                id = id,
                name = location.name,
                category = location.category,
                lat = location.lat,
                lng = location.lng,
                description = location.description,
                image = location.image,
                address = location.address,
                rating = location.rating,
                icon = location.icon,
                color = location.color,
            )
            locations[id] = updated
            return updated
        }

        override suspend fun delete(id: String): Boolean = locations.remove(id) != null
    }
}
