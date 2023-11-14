package io.billie.order.viewmodel

import java.util.UUID

data class ProductRequest(
    var name: String,
    var organisationId: UUID,
)