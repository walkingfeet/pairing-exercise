package io.billie.order.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Order(
    val id: UUID,
    val buyerId: UUID,
    val merchantId: UUID,
    val totalPrice: BigDecimal,
    val status: OrderStatus,
    val created: Instant,
    val updated: Instant
)