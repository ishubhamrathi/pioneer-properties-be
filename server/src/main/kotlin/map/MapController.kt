package com.pioneer.map

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

class MapController(
    private val service: MapService,
) {
    fun Route.routes() {
        route("/api/locations") {
            get {
                val bounds = call.request.queryParameters["bounds"]?.toBounds()
                    ?: throw IllegalArgumentException("Missing bounds query parameter")
                val zoom = call.request.queryParameters["zoom"]?.toIntOrNull()

                call.respond(service.getLocations(bounds, zoom))
            }

            get("/search") {
                val query = call.request.queryParameters["q"]
                    ?: throw IllegalArgumentException("Missing q query parameter")
                val search = LocationSearchDto(
                    query = query,
                    lat = call.request.queryParameters["lat"]?.toDoubleOrNull(),
                    lng = call.request.queryParameters["lng"]?.toDoubleOrNull(),
                    radiusKm = call.request.queryParameters["radius"]?.toDoubleOrNull(),
                )

                call.respond(service.searchLocations(search))
            }

            get("/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing location id")
                val location = service.getLocation(id)

                if (location == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(location)
                }
            }
        }
    }

    private fun String.toBounds(): BoundsDto {
        val parts = split(",").map { it.trim().toDoubleOrNull() }
        require(parts.size == 4 && parts.all { it != null }) {
            "bounds must use minLat,minLng,maxLat,maxLng"
        }

        return BoundsDto(
            minLat = parts[0]!!,
            minLng = parts[1]!!,
            maxLat = parts[2]!!,
            maxLng = parts[3]!!,
        )
    }
}
