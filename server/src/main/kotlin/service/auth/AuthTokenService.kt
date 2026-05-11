package com.pioneer.service.auth

import com.pioneer.dao.auth.UserRecord
import com.pioneer.domain.auth.AuthTokenClaims
import java.time.Clock
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthTokenService(
    private val secret: String,
    private val clock: Clock = Clock.systemUTC(),
    private val tokenTtlSeconds: Long = 24 * 60 * 60,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun createToken(user: UserRecord): String {
        val claims = AuthTokenClaims(
            userId = user.id,
            email = user.email,
            role = user.role,
            expiresAt = clock.instant().epochSecond + tokenTtlSeconds,
        )
        val payload = encoder.encodeToString(json.encodeToString(claims).toByteArray(Charsets.UTF_8))
        val signature = sign(payload)

        return "$payload.$signature"
    }

    fun verify(token: String): AuthTokenClaims? {
        val parts = token.split(".")
        if (parts.size != 2) return null

        val payload = parts[0]
        val signature = parts[1]
        if (signature != sign(payload)) return null

        val claims = runCatching {
            json.decodeFromString<AuthTokenClaims>(decoder.decode(payload).toString(Charsets.UTF_8))
        }.getOrNull() ?: return null

        return claims.takeIf { it.expiresAt > clock.instant().epochSecond }
    }

    private fun sign(payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))

        return encoder.encodeToString(mac.doFinal(payload.toByteArray(Charsets.UTF_8)))
    }
}
