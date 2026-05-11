package com.pioneer.controller.map

import com.pioneer.domain.map.LocationCreateRequestDto
import com.pioneer.domain.map.LocationUpdateRequestDto
import com.pioneer.service.map.LocationManagementService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

class LocationManagementController(
    private val service: LocationManagementService,
) {
    fun Route.routes() {
        route("/api/locations") {
            post {
                val request = call.receive<LocationCreateRequestDto>()
                val location = service.createLocation(request)

                call.respond(HttpStatusCode.Created, location)
            }

            put("/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing location id")
                val location = service.updateLocation(id, call.receive())

                if (location == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    call.respond(location)
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing location id")

                if (service.deleteLocation(id)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
