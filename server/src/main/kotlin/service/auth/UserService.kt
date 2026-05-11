package com.pioneer.service.auth

import com.pioneer.dao.auth.UserDao
import com.pioneer.dao.auth.UserRecord
import com.pioneer.domain.auth.AuthResponseDto
import com.pioneer.domain.auth.AuthTokenClaims
import com.pioneer.domain.auth.SignInRequestDto
import com.pioneer.domain.auth.SignUpRequestDto
import com.pioneer.domain.auth.UserDto
import com.pioneer.domain.auth.UserRole

interface UserService {
    suspend fun signUp(request: SignUpRequestDto): AuthResponseDto

    suspend fun signIn(request: SignInRequestDto): AuthResponseDto

    suspend fun currentUser(token: String): UserDto?

    suspend fun listUsers(token: String): List<UserDto>?

    suspend fun updateRole(token: String, id: String, role: UserRole): UserDto?
}

class DefaultUserService(
    private val dao: UserDao,
    private val passwordHasher: PasswordHasher,
    private val tokenService: AuthTokenService,
) : UserService {
    override suspend fun signUp(request: SignUpRequestDto): AuthResponseDto {
        val email = request.email.normalizedEmail()
        request.validate()

        val user = dao.create(
            email = email,
            name = request.name.trim(),
            role = request.role,
            passwordHash = passwordHasher.hash(request.password),
        ) ?: throw IllegalArgumentException("Email is already registered")

        return user.toAuthResponse()
    }

    override suspend fun signIn(request: SignInRequestDto): AuthResponseDto {
        val user = dao.findByEmail(request.email.normalizedEmail())
            ?: throw IllegalArgumentException("Invalid email or password")

        if (!passwordHasher.verify(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        return user.toAuthResponse()
    }

    override suspend fun currentUser(token: String): UserDto? {
        val claims = tokenService.verify(token) ?: return null

        return dao.findById(claims.userId)?.toDto()
    }

    override suspend fun listUsers(token: String): List<UserDto>? {
        requireAdmin(token) ?: return null

        return dao.findAll().map { it.toDto() }
    }

    override suspend fun updateRole(token: String, id: String, role: UserRole): UserDto? {
        requireAdmin(token) ?: return null

        return dao.updateRole(id, role)?.toDto()
    }

    private fun UserRecord.toAuthResponse(): AuthResponseDto = AuthResponseDto(
        token = tokenService.createToken(this),
        user = toDto(),
    )

    private suspend fun requireAdmin(token: String): AuthTokenClaims? {
        val claims = tokenService.verify(token) ?: return null

        return claims.takeIf { it.role == UserRole.ADMIN }
    }

    private fun UserRecord.toDto(): UserDto = UserDto(
        id = id,
        email = email,
        name = name,
        role = role,
    )

    private fun SignUpRequestDto.validate() {
        require(email.normalizedEmail().contains("@")) { "Valid email is required" }
        require(name.isNotBlank()) { "Name is required" }
        require(password.length >= 8) { "Password must be at least 8 characters" }
    }

    private fun String.normalizedEmail(): String = trim().lowercase()
}
