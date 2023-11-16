package io.billie.order.data

import io.billie.SpringIntegrationTest
import io.billie.order.model.Order
import io.billie.order.model.OrderStatus
import io.billie.organisations.data.OrganisationRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OrderRepositoryTest: SpringIntegrationTest() {

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var organisationRepository: OrganisationRepository

    @Test
    fun `should create and find order by id`() {
        val buyerOrganisationId = organisationRepository.create(DataTemplates.organisationRequestTemplate)
        val merchantOrganisationId = organisationRepository.create(DataTemplates.organisationRequestTemplate)

        val totalPrice = BigDecimal("100.012")

        val orderId = orderRepository.createOrder(
            buyerOrganisationId, merchantOrganisationId, totalPrice, OrderStatus.IN_PROGRESS
        )

        // Find the created order by ID
        val foundOrder = orderRepository.findOrderById(orderId)
        assertNotNull(foundOrder)

        // Assert that the found order matches the expected values
        val expectedOrder = Order(
            id = orderId,
            buyerId = buyerOrganisationId,
            merchantId = merchantOrganisationId,
            totalPrice = totalPrice,
            status = OrderStatus.IN_PROGRESS,
            // Ignore as default values
            created = foundOrder.created,
            updated = foundOrder.updated
        )

        assertEquals(expectedOrder, foundOrder)
    }

    @Test
    fun `should update order status`() {
        val buyerOrganisationId = organisationRepository.create(DataTemplates.organisationRequestTemplate)
        val merchantOrganisationId = organisationRepository.create(DataTemplates.organisationRequestTemplate)
        val orderId = orderRepository.createOrder(
            buyerOrganisationId, merchantOrganisationId, BigDecimal("150.00"), OrderStatus.IN_PROGRESS
        )

        orderRepository.updateOrderStatus(orderId, OrderStatus.SHIPPED)

        val updatedOrder = orderRepository.findOrderById(orderId)
        assertNotNull(updatedOrder)

        assertEquals(OrderStatus.SHIPPED, updatedOrder.status)
    }
}