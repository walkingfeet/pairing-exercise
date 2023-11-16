package io.billie.order.viewmodel

import io.billie.order.model.OrderStatus

data class ShippingOrderResponse(val orderStatus: OrderStatus, val description: String) {
}


