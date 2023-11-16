package io.billie.order.viewmodel

import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.Size

data class ShippingOrderRequest(
    val orderId: UUID,
    @Valid
    @Size(min = 1)
    val shippedProductList: List<ShippedProduct>
) {
}

data class ShippedProduct(
    val productId: UUID,
    @Min(1)
    val amount: Int
)