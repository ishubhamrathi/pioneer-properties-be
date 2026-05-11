package com.pioneer.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequestDto(
    val email: String,
    val password: String,
    val name: String,
    val role: UserRole = UserRole.USER,
)

@Serializable
data class SignInRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val user: UserDto,
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
)

@Serializable
data class UpdateUserRoleRequestDto(
    val role: UserRole,
)

@Serializable
data class AuthTokenClaims(
    val userId: String,
    val email: String,
    val role: UserRole,
    val expiresAt: Long,
)

@Serializable
enum class UserRole {
    USER,
    ADMIN,
}
