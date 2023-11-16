package io.billie.order.model
// DN: Enum for future expansion - as example - it can be NEW - when awaiting merchant to confirm it can deliver goods
//      and IN_PROGRESS - before delivery.For Exercise NEW is not used, to avoid dead code - IN_PROGRESS - first status
enum class OrderStatus {
    IN_PROGRESS,
    SHIPPED
}