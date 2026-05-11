package com.pioneer.service.auth

import com.pioneer.dao.auth.UserDao
import com.pioneer.dao.auth.UserRecord
import com.pioneer.domain.auth.SignInRequestDto
import com.pioneer.domain.auth.SignUpRequestDto
import com.pioneer.domain.auth.UserRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking

class UserServiceTest {
    @Test
    fun `sign up creates user and returns token with role`() {
        runBlocking {
            val service = createService()

            val response = service.signUp(
                SignUpRequestDto(
                    email = " Admin@Example.com ",
                    password = "password123",
                    name = " Admin User ",
                    role = UserRole.ADMIN,
                ),
            )

            assertEquals("admin@example.com", response.user.email)
            assertEquals("Admin User", response.user.name)
            assertEquals(UserRole.ADMIN, response.user.role)
            assertNotNull(response.token)
        }
    }

    @Test
    fun `sign in rejects invalid password`() {
        runBlocking {
            val service = createService()
            service.signUp(
                SignUpRequestDto(
                    email = "user@example.com",
                    password = "password123",
                    name = "User",
                ),
            )

            assertFailsWith<IllegalArgumentException> {
                service.signIn(
                    SignInRequestDto(
                        email = "user@example.com",
                        password = "wrong-password",
                    ),
                )
            }
        }
    }

    @Test
    fun `admin token can list users`() {
        runBlocking {
            val service = createService()
            val admin = service.signUp(
                SignUpRequestDto(
                    email = "admin@example.com",
                    password = "password123",
                    name = "Admin",
                    role = UserRole.ADMIN,
                ),
            )

            val users = service.listUsers(admin.token)

            assertEquals(1, users?.size)
            assertEquals(UserRole.ADMIN, users?.first()?.role)
        }
    }

    private fun createService(): DefaultUserService {
        val passwordHasher = PasswordHasher()

        return DefaultUserService(
            dao = FakeUserDao(),
            passwordHasher = passwordHasher,
            tokenService = AuthTokenService("test-secret"),
        )
    }

    private class FakeUserDao : UserDao {
        private var nextId = 1
        private val users = mutableMapOf<String, UserRecord>()

        override suspend fun create(
            email: String,
            name: String,
            role: UserRole,
            passwordHash: String,
        ): UserRecord? {
            if (users.values.any { it.email == email }) return null

            val user = UserRecord(
                id = (nextId++).toString(),
                email = email,
                name = name,
                role = role,
                passwordHash = passwordHash,
            )
            users[user.id] = user
            return user
        }

        override suspend fun findByEmail(email: String): UserRecord? =
            users.values.firstOrNull { it.email == email }

        override suspend fun findById(id: String): UserRecord? = users[id]

        override suspend fun findAll(): List<UserRecord> = users.values.toList()

        override suspend fun updateRole(id: String, role: UserRole): UserRecord? {
            val user = users[id] ?: return null
            val updated = user.copy(role = role)
            users[id] = updated
            return updated
        }
    }
}
