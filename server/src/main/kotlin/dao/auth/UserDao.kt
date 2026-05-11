package com.pioneer.dao.auth

import com.mongodb.MongoWriteException
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates
import com.pioneer.domain.auth.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.bson.types.ObjectId

data class UserRecord(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val passwordHash: String,
)

interface UserDao {
    suspend fun create(email: String, name: String, role: UserRole, passwordHash: String): UserRecord?

    suspend fun findByEmail(email: String): UserRecord?

    suspend fun findById(id: String): UserRecord?

    suspend fun findAll(): List<UserRecord>

    suspend fun updateRole(id: String, role: UserRole): UserRecord?
}

class MongoUserDao(
    database: MongoDatabase,
) : UserDao {
    private val collection: MongoCollection<Document> = database.getCollection("users")

    init {
        collection.createIndex(Indexes.ascending("email"), IndexOptions().unique(true))
    }

    override suspend fun create(
        email: String,
        name: String,
        role: UserRole,
        passwordHash: String,
    ): UserRecord? = withContext(Dispatchers.IO) {
        val document = Document()
            .append("email", email)
            .append("name", name)
            .append("role", role.name)
            .append("passwordHash", passwordHash)

        try {
            collection.insertOne(document)
            document.toUserRecord()
        } catch (_: MongoWriteException) {
            null
        }
    }

    override suspend fun findByEmail(email: String): UserRecord? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("email", email)).first()?.toUserRecord()
    }

    override suspend fun findById(id: String): UserRecord? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("_id", ObjectId(id))).first()?.toUserRecord()
    }

    override suspend fun findAll(): List<UserRecord> = withContext(Dispatchers.IO) {
        collection.find().map { it.toUserRecord() }.toList()
    }

    override suspend fun updateRole(id: String, role: UserRole): UserRecord? = withContext(Dispatchers.IO) {
        collection.findOneAndUpdate(
            Filters.eq("_id", ObjectId(id)),
            Updates.set("role", role.name),
        )

        findById(id)
    }

    private fun Document.toUserRecord(): UserRecord = UserRecord(
        id = getObjectId("_id").toHexString(),
        email = getString("email"),
        name = getString("name"),
        role = UserRole.valueOf(getString("role")),
        passwordHash = getString("passwordHash"),
    )
}
