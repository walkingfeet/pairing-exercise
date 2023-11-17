package io.billie.order.viewmodel

import java.time.Instant
import java.util.UUID

data class ProductResponse(
    val id: UUID,
    val name: String,
    val organisationId: UUID,
    val created: Instant,
    val updated: Instant
) {
}