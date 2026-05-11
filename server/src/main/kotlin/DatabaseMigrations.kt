package com.pioneer

import io.ktor.server.application.Application
import io.ktor.server.config.tryGetString
import org.flywaydb.core.Flyway

fun Application.configureDatabaseMigrations() {
    val url = environment.config.tryGetString("db.postgres.url")
        ?.takeIf(String::isNotBlank)
        ?: System.getenv("SUPABASE_DB_URL")
        ?: return

    val user = environment.config.tryGetString("db.postgres.user")
        ?.takeIf(String::isNotBlank)
        ?: System.getenv("SUPABASE_DB_USER")

    val password = environment.config.tryGetString("db.postgres.password")
        ?.takeIf(String::isNotBlank)
        ?: System.getenv("SUPABASE_DB_PASSWORD")

    Flyway.configure()
        .dataSource(url, user, password)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .load()
        .migrate()
}
