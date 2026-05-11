package com.pioneer.dao.map

import com.pioneer.domain.map.BoundsDto
import com.pioneer.domain.map.LocationDto
import com.pioneer.domain.map.LocationSearchDto
import com.pioneer.domain.map.LocationUpdateRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL

class JooqMapDao(
    private val dsl: DSLContext,
    private val tableName: String = "locations",
) : MapDao {
    private val idField = DSL.field(DSL.name("id"), String::class.java)
    private val nameField = DSL.field(DSL.name("name"), String::class.java)
    private val categoryField = DSL.field(DSL.name("category"), String::class.java)
    private val latField = DSL.field("ST_Y(geom)", Double::class.java).`as`("lat")
    private val lngField = DSL.field("ST_X(geom)", Double::class.java).`as`("lng")
    private val descriptionField = DSL.field(DSL.name("description"), String::class.java)
    private val imageField = DSL.field(DSL.name("image"), String::class.java)
    private val addressField = DSL.field(DSL.name("address"), String::class.java)
    private val ratingField = DSL.field(DSL.name("rating"), Double::class.java)
    private val iconField = DSL.field(DSL.name("icon"), String::class.java)
    private val colorField = DSL.field(DSL.name("color"), String::class.java)

    override suspend fun findInBounds(bounds: BoundsDto, zoom: Int?, limit: Int): List<LocationDto> =
        queryLocations(limit) { table ->
            """
            ST_Intersects(
                geom,
                ST_SetSRID(ST_MakeEnvelope(?, ?, ?, ?), 4326)
            )
            """.trimIndent() to listOf(bounds.minLng, bounds.minLat, bounds.maxLng, bounds.maxLat)
        }

    override suspend fun findById(id: String): LocationDto? =
        withContext(Dispatchers.IO) {
            val table = DSL.table(DSL.name(tableName))
            dsl
                .select(
                    idField,
                    nameField,
                    categoryField,
                    latField,
                    lngField,
                    descriptionField,
                    imageField,
                    addressField,
                    ratingField,
                    iconField,
                    colorField,
                )
                .from(table)
                .where(DSL.field(DSL.name("id")).eq(id))
                .limit(1)
                .fetchOne()
                ?.toLocationDto()
        }

    override suspend fun search(search: LocationSearchDto, limit: Int): List<LocationDto> =
        queryLocations(limit) {
            val conditions = mutableListOf<String>()
            val values = mutableListOf<Any>()

            conditions += "(name ILIKE ? OR address ILIKE ? OR category ILIKE ?)"
            val likeQuery = "%${search.query}%"
            values += likeQuery
            values += likeQuery
            values += likeQuery

            if (search.lat != null && search.lng != null && search.radiusKm != null) {
                conditions += """
                    ST_DWithin(
                        geom::geography,
                        ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                        ?
                    )
                """.trimIndent()
                values += search.lng
                values += search.lat
                values += search.radiusKm * 1000.0
            }

            conditions.joinToString(" AND ") to values
        }

    override suspend fun create(location: LocationDto): LocationDto =
        withContext(Dispatchers.IO) {
            dsl.execute(
                """
                INSERT INTO ${tableSql()} (
                    id,
                    name,
                    category,
                    geom,
                    description,
                    image,
                    address,
                    rating,
                    icon,
                    color
                )
                VALUES (?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326), ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                location.id,
                location.name,
                location.category,
                location.lng,
                location.lat,
                location.description,
                location.image,
                location.address,
                location.rating,
                location.icon,
                location.color,
            )

            requireNotNull(findById(location.id)) { "Created location could not be loaded" }
        }

    override suspend fun update(id: String, location: LocationUpdateRequestDto): LocationDto? =
        withContext(Dispatchers.IO) {
            val updated = dsl.execute(
                """
                UPDATE ${tableSql()}
                SET
                    name = ?,
                    category = ?,
                    geom = ST_SetSRID(ST_MakePoint(?, ?), 4326),
                    description = ?,
                    image = ?,
                    address = ?,
                    rating = ?,
                    icon = ?,
                    color = ?
                WHERE id = ?
                """.trimIndent(),
                location.name,
                location.category,
                location.lng,
                location.lat,
                location.description,
                location.image,
                location.address,
                location.rating,
                location.icon,
                location.color,
                id,
            )

            if (updated == 0) null else findById(id)
        }

    override suspend fun delete(id: String): Boolean =
        withContext(Dispatchers.IO) {
            dsl.execute("DELETE FROM ${tableSql()} WHERE id = ?", id) > 0
        }

    private suspend fun queryLocations(
        limit: Int,
        whereSql: (table: org.jooq.Table<*>) -> Pair<String, List<Any>>,
    ): List<LocationDto> = withContext(Dispatchers.IO) {
            val table = DSL.table(DSL.name(tableName))
            val (sql, values) = whereSql(table)

            dsl
                .select(
                    idField,
                    nameField,
                    categoryField,
                    latField,
                    lngField,
                    descriptionField,
                    imageField,
                    addressField,
                    ratingField,
                    iconField,
                    colorField,
                )
                .from(table)
                .where(DSL.condition(sql, *values.toTypedArray()))
                .limit(limit)
                .fetch()
                .map { it.toLocationDto() }
    }

    private fun Record.toLocationDto(): LocationDto = LocationDto(
        id = get(idField),
        name = get(nameField),
        category = get(categoryField),
        lat = get("lat", Double::class.java),
        lng = get("lng", Double::class.java),
        description = get(descriptionField),
        image = get(imageField),
        address = get(addressField),
        rating = get(ratingField),
        icon = get(iconField),
        color = get(colorField),
    )

    private fun tableSql(): String = dsl.render(DSL.table(DSL.name(tableName)))
}
