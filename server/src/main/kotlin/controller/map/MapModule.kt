package com.pioneer.controller.map

import com.pioneer.createPostgresDslContext
import com.pioneer.dao.map.JooqMapDao
import com.pioneer.dao.map.MapDao
import com.pioneer.service.map.DefaultLocationManagementService
import com.pioneer.service.map.DefaultMapService
import com.pioneer.service.map.LocationManagementService
import com.pioneer.service.map.MapService
import io.ktor.server.application.Application
import io.ktor.server.config.tryGetString
import io.ktor.server.routing.routing

fun Application.configureMapController() {
    val dsl = createPostgresDslContext() ?: return
    val tableName = environment.config.tryGetString("db.postgres.locationsTable") ?: "locations"
    val dao: MapDao = JooqMapDao(dsl, tableName)
    val service: MapService = DefaultMapService(dao)
    val controller = MapController(service)
    val managementService: LocationManagementService = DefaultLocationManagementService(dao)
    val managementController = LocationManagementController(managementService)

    routing {
        with(controller) {
            routes()
        }
        with(managementController) {
            routes()
        }
    }
}
