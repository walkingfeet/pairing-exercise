package io.billie.order.viewmodel

import java.util.UUID

data class CreateProductRequest(
    var name: String,
    var organisationId: UUID,
)