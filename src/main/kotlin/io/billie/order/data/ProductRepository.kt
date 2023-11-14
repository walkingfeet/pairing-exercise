package io.billie.order.data

import io.billie.order.viewmodel.ProductRequest
import io.billie.order.viewmodel.ProductResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

@Repository
class ProductRepository{

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    fun createProduct(productRequest: ProductRequest): UUID {
        // DN: Other values are controlled by database as in organisation logic
        val sql = """
            INSERT INTO orders_schema.products (name, organisation_id)
            VALUES (:name, :organisationId)
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("name", productRequest.name)
            .addValue("organisationId", productRequest.organisationId)

        val keyHolder: KeyHolder = GeneratedKeyHolder()

        jdbcTemplate.update(
            sql,
            params,
            keyHolder,
            arrayOf("id")
        )

        return keyHolder.getKeyAs(UUID::class.java)!!
    }

    // DN: Method to expand in future - for example we are using some list in UI
    fun findProductsByOrganisationId(organisationId: UUID): List<ProductResponse> {
        val sql = "   SELECT id, name, created, updated " +
                "            FROM orders_schema.products " +
                "            WHERE organisation_id = :organisation_id " +
                "            ORDER BY created DESC "

        val params = mapOf("organisation_id" to organisationId)

        return jdbcTemplate.query(
            sql,
            params,
            ProductRowMapper()
        )
    }

    private class ProductRowMapper : RowMapper<ProductResponse> {
        override fun mapRow(rs: ResultSet, rowNum: Int): ProductResponse {
            return ProductResponse(
                id = UUID.fromString(rs.getString("id")),
                name = rs.getString("name"),
                created = rs.getTimestamp("created").toInstant(),
                updated = rs.getTimestamp("updated").toInstant()
            )
        }
    }
}