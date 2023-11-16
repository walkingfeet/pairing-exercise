package io.billie.order.viewmodel

import java.math.BigDecimal
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.validation.constraints.Size

data class CheckoutOrderRequest(
    val buyerId: UUID,
    val merchantId: UUID,
    val totalPrice: BigDecimal,
    @Valid
    @Size(min = 1)
    val productList: List<ProductInOrder>
)


data class ProductInOrder(
    val productId: UUID,
    @Min(1)
    val amount: Int
)