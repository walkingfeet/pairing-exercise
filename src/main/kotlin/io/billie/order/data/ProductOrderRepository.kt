package io.billie.order.data

import io.billie.order.model.ProductOrder
import io.billie.order.viewmodel.ProductInOrder
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

@Repository
class ProductOrderRepository(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {

    fun createOrderProducts(orderId: UUID, orderedItems: List<ProductInOrder>) {
        val sql = "INSERT INTO orders_schema.product_orders (id, order_id, product_id, products_amount_in_order, " +
                "products_amount_shipped, created, updated) VALUES (:id, :orderId, :productId, :productsAmountInOrder, " +
                ":productsAmountShipped, :created, :updated)"

        val batchArgs = orderedItems.map {
            mapOf(
                "orderId" to orderId,
                "productId" to it.productId,
                "productsAmountInOrder" to it.amount,
                "productsAmountShipped" to 0,
            )
        }.toTypedArray()

        namedParameterJdbcTemplate.batchUpdate(sql, batchArgs)
    }

    /**
     * Method with locking rows for further update
     * Usage only in transaction
     */
    fun selectToUpdate(orderId: UUID, productList: List<UUID>): List<ProductOrder> {
        val sql = "SELECT id, order_id, product_id, products_amount_in_order, products_amount_shipped, created, updated " +
                "FROM orders_schema.product_orders WHERE order_id = :orderId AND product_id IN (:productList) FOR UPDATE"

        val paramMap = mapOf(
            "orderId" to orderId,
            "productList" to productList
        )

        return namedParameterJdbcTemplate.query(sql, paramMap, ProductOrderRowMapper())
    }

    fun batchUpdateProductsAmountShipped(orderId: UUID, productUpdates: List<ProductUpdate>) {
        val sql = "UPDATE orders_schema.product_orders " +
                "SET products_amount_shipped = products_amount_shipped + :newProductsAmountShipped " +
                "WHERE order_id = :orderId AND product_id = :productId"

        val batchArgs = productUpdates.map {
            mapOf(
                "orderId" to orderId,
                "productId" to it.productId,
                "additionalAmountShipped" to it.additionalAmountShipped
            )
        }.toTypedArray()

        namedParameterJdbcTemplate.batchUpdate(sql, batchArgs)
    }

    /**
     * Returns true if all products are shipped and false otherwise
     * Need to switch status of the order
     */
    fun isAllProductsInOrderAreShipped(orderId: UUID) : Boolean {
        val sql = "SELECT EXISTS (" +
                "  SELECT 1 " +
                "  FROM orders_schema.product_orders " +
                "  WHERE order_id = :orderId AND products_amount_shipped != products_amount_in_order" +
                ")"

        val paramMap = mapOf("orderId" to orderId)

        return namedParameterJdbcTemplate.queryForObject(sql, paramMap, Boolean::class.java) ?: false
    }

    data class ProductUpdate(val productId: UUID, val additionalAmountShipped: Int)



    fun findProductOrdersByOrderId(orderId: UUID): List<ProductOrder> {
        val sql = "SELECT id, order_id, product_id, products_amount_in_order, products_amount_shipped, created, updated " +
                "FROM orders_schema.product_orders WHERE order_id = :orderId"

        val paramMap = mapOf("orderId" to orderId)

        return namedParameterJdbcTemplate.query(sql, paramMap, ProductOrderRowMapper())
    }

    private class ProductOrderRowMapper : RowMapper<ProductOrder> {
        override fun mapRow(rs: ResultSet, rowNum: Int): ProductOrder {
            return ProductOrder(
                id = rs.getObject("id", UUID::class.java),
                orderId = rs.getObject("order_id", UUID::class.java),
                productId = rs.getObject("product_id", UUID::class.java),
                productsAmountInOrder = rs.getInt("products_amount_in_order"),
                productsAmountShipped = rs.getInt("products_amount_shipped"),
                created = rs.getTimestamp("created").toInstant(),
                updated = rs.getTimestamp("updated").toInstant()
            )
        }
    }
}