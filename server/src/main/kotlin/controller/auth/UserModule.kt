package com.pioneer.controller.auth

import com.pioneer.connectToMongoDB
import com.pioneer.dao.auth.MongoUserDao
import com.pioneer.dao.auth.UserDao
import com.pioneer.service.auth.AuthTokenService
import com.pioneer.service.auth.DefaultUserService
import com.pioneer.service.auth.PasswordHasher
import com.pioneer.service.auth.UserService
import io.ktor.server.application.Application
import io.ktor.server.config.tryGetString
import io.ktor.server.routing.routing

fun Application.configureUserController() {
    val database = connectToMongoDB()
    val dao: UserDao = MongoUserDao(database)
    val tokenSecret = environment.config.tryGetString("auth.tokenSecret")
        ?.takeIf(String::isNotBlank)
        ?: System.getenv("AUTH_TOKEN_SECRET")
        ?: "change-this-local-dev-secret"
    val tokenTtlSeconds = environment.config.tryGetString("auth.tokenTtlSeconds")?.toLongOrNull()
        ?: System.getenv("AUTH_TOKEN_TTL_SECONDS")?.toLongOrNull()
        ?: 24 * 60 * 60
    val service: UserService = DefaultUserService(
        dao = dao,
        passwordHasher = PasswordHasher(),
        tokenService = AuthTokenService(
            secret = tokenSecret,
            tokenTtlSeconds = tokenTtlSeconds,
        ),
    )
    val controller = UserController(service)

    routing {
        with(controller) {
            routes()
        }
    }
}
