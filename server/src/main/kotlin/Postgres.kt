package com.pioneer

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.config.tryGetString
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL

fun Application.createPostgresDslContext(): DSLContext? {
    val url = environment.config.tryGetString("db.postgres.url")
        ?.takeIf(String::isNotBlank)
        ?: System.getenv("SUPABASE_DB_URL")
        ?: return null

    val user = environment.config.tryGetString("db.postgres.user")
        ?.takeIf(String::isNotBlank)
        ?: System.getenv("SUPABASE_DB_USER")

    val password = environment.config.tryGetString("db.postgres.password")
        ?.takeIf(String::isNotBlank)
        ?: System.getenv("SUPABASE_DB_PASSWORD")

    val maxPoolSize = environment.config.tryGetString("db.postgres.maxPoolSize")?.toIntOrNull()
        ?: System.getenv("SUPABASE_DB_MAX_POOL_SIZE")?.toIntOrNull()
        ?: 10

    val dataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = url
            username = user
            this.password = password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = maxPoolSize
            poolName = "supabase-postgis"
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        },
    )

    monitor.subscribe(ApplicationStopped) {
        dataSource.close()
    }

    return DSL.using(dataSource, SQLDialect.POSTGRES)
}
