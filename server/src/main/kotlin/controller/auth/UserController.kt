package com.pioneer.controller.auth

import com.pioneer.domain.auth.UpdateUserRoleRequestDto
import com.pioneer.service.auth.UserService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

class UserController(
    private val service: UserService,
) {
    fun Route.routes() {
        route("/api/auth") {
            post("/signup") {
                call.respond(HttpStatusCode.Created, service.signUp(call.receive()))
            }

            post("/signin") {
                call.respond(service.signIn(call.receive()))
            }

            get("/me") {
                val token = call.bearerToken()
                val user = token?.let { service.currentUser(it) }

                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                } else {
                    call.respond(user)
                }
            }
        }

        route("/api/users") {
            get {
                val users = call.bearerToken()?.let { service.listUsers(it) }

                if (users == null) {
                    call.respond(HttpStatusCode.Forbidden)
                } else {
                    call.respond(users)
                }
            }

            put("/{id}/role") {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing user id")
                val request = call.receive<UpdateUserRoleRequestDto>()
                val user = call.bearerToken()?.let { service.updateRole(it, id, request.role) }

                if (user == null) {
                    call.respond(HttpStatusCode.Forbidden)
                } else {
                    call.respond(user)
                }
            }
        }
    }

    private fun io.ktor.server.application.ApplicationCall.bearerToken(): String? =
        request.headers[HttpHeaders.Authorization]
            ?.removePrefix("Bearer ")
            ?.takeIf { it.isNotBlank() }
}
