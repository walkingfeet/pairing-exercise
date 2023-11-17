package io.billie.order.resource

import io.billie.order.service.OrderService
import io.billie.order.viewmodel.CheckoutOrderRequest
import io.billie.order.viewmodel.CreateProductRequest
import io.billie.order.viewmodel.OrderStatusEntity
import io.billie.order.viewmodel.ShippingOrderRequest
import io.billie.organisations.viewmodel.Entity
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/orders")
class OrderResource(private val orderService: OrderService) {

    @PostMapping
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Creating the new order",
                content = [
                    (Content(
                        mediaType = "application/json",
                        array = (ArraySchema(schema = Schema(implementation = Entity::class)))
                    ))]
            ),
            ApiResponse(responseCode = "400", description = "Bad request", content = [Content()])]
    )
    fun checkoutOrder(@RequestBody @Valid checkoutOrderRequest: CheckoutOrderRequest): Entity {
        return Entity(orderService.checkoutOrder(checkoutOrderRequest))
    }

    @PostMapping("/make-shipment")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Notify about shipment. Returns if order is delivered",
                content = [
                    (Content(
                        mediaType = "application/json",
                        array = (ArraySchema(schema = Schema(implementation = Entity::class)))
                    ))]
            ),
            ApiResponse(responseCode = "400", description = "Bad request", content = [Content()])]
    )
    fun makeShipment(@RequestBody @Valid shippingOrderRequest: ShippingOrderRequest): OrderStatusEntity {
        return OrderStatusEntity(orderService.shipOrder(shippingOrderRequest))
    }

}