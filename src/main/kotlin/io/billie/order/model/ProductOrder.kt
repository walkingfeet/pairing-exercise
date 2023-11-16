package io.billie.order.model

import java.time.Instant
import java.util.UUID


data class ProductOrder(
    val id: UUID,
    val orderId: UUID,
    val productId: UUID,
    val productsAmountInOrder: Int,
    val productsAmountShipped: Int,
    val created: Instant,
    val updated: Instant
)