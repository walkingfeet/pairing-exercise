package io.billie.order.data

import io.billie.order.model.Order
import io.billie.order.model.OrderStatus
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.ResultSet
import java.util.UUID

@Repository
class OrderRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {


    fun createOrder(buyerId: UUID,
                    merchantId: UUID,
                    totalPrice: BigDecimal,
                    status: OrderStatus): UUID {
        val sql = """INSERT INTO orders_schema.orders(buyer_id, merchant_id, total_price, status)
            VALUES (:buyerId, :merchantId, :totalPrice, :status)
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("buyerId", buyerId)
            .addValue("merchantId", merchantId)
            .addValue("totalPrice", totalPrice)
            .addValue("status", status.toString())

        val keyHolder: KeyHolder = GeneratedKeyHolder()

        jdbcTemplate.update(sql, params, keyHolder, arrayOf("id"))

        return keyHolder.getKeyAs(UUID::class.java)!!
    }

    fun updateOrderStatus(orderId: UUID, orderStatus: OrderStatus) {
        val sql = """UPDATE orders_schema.orders 
                SET status = :newStatus,
                updated = now()
                WHERE id = :orderId""".trimIndent()

        val paramMap = mapOf("orderId" to orderId, "newStatus" to OrderStatus.SHIPPED.toString())

        jdbcTemplate.update(sql, paramMap)
    }

    fun findOrderById(orderId: UUID): Order? {
        val sql = """SELECT id, buyer_id, merchant_id, total_price, status, created, updated 
                FROM orders_schema.orders 
                WHERE id = :orderId""".trimIndent()

        val paramMap = mapOf("orderId" to orderId)

        return try {
            jdbcTemplate.queryForObject(sql, paramMap, OrderRowMapper())
        } catch (ex: EmptyResultDataAccessException) {
            return null
        }
    }

    private class OrderRowMapper : RowMapper<Order> {
        override fun mapRow(rs: ResultSet, rowNum: Int): Order {
            return Order(
                id = rs.getObject("id", UUID::class.java),
                buyerId = rs.getObject("buyer_id", UUID::class.java),
                merchantId = rs.getObject("merchant_id", UUID::class.java),
                totalPrice = rs.getBigDecimal("total_price"),
                status = OrderStatus.valueOf(rs.getString("status")),
                created = rs.getTimestamp("created").toInstant(),
                updated = rs.getTimestamp("updated").toInstant()
            )
        }
    }
}