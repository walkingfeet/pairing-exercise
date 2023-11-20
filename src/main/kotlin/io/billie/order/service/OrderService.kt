package io.billie.order.service

import io.billie.order.data.OrderNotificationRepository
import io.billie.order.data.OrderRepository
import io.billie.order.data.ProductOrderRepository
import io.billie.order.data.ProductRepository
import io.billie.order.exception.OrderNotFoundException
import io.billie.order.exception.OrganisationNotFoundException
import io.billie.order.exception.ProductIsNotFromMerchantInOrder
import io.billie.order.exception.ProductNotFoundException
import io.billie.order.exception.TotalShippedIsAboveThanInOrder
import io.billie.order.model.OrderStatus
import io.billie.order.viewmodel.CheckoutOrderRequest
import io.billie.order.viewmodel.ShippingOrderRequest
import io.billie.organisations.data.OrganisationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalStateException
import java.util.UUID

@Service
class OrderService(private val orderRepository: OrderRepository,
                   private val productOrderRepository: ProductOrderRepository,
                   private val productRepository: ProductRepository,
                   private val organisationRepository: OrganisationRepository,
                   private val notificationRepository: OrderNotificationRepository) {


    @Transactional
    fun checkoutOrder(orderRequest: CheckoutOrderRequest): UUID {
        // Checking if organisations exists to throw business error
        organisationRepository.findOrganisationById(orderRequest.buyerId) ?: throw OrganisationNotFoundException()
        organisationRepository.findOrganisationById(orderRequest.merchantId) ?: throw OrganisationNotFoundException()

        // DN: I also could make list of errors for better user experience
        orderRequest.productList.forEach{
           val product = productRepository.findProductById(it.productId) ?: throw ProductNotFoundException()
            if(product.organisationId != orderRequest.merchantId) {
                throw ProductIsNotFromMerchantInOrder()
            }
        }

        // DN: Make status in progress - as far as merchant should not approve it
        val orderId = orderRepository.createOrder(orderRequest.buyerId, orderRequest.merchantId, orderRequest.totalPrice, OrderStatus.IN_PROGRESS)
        productOrderRepository.createOrderProducts(orderId, orderRequest.productList)
        return orderId
    }

    /**
     * Returns if order is shipped
     */
    @Transactional
    fun shipOrder(shippingOrderRequest: ShippingOrderRequest): OrderStatus {
        val order = orderRepository.findOrderById(shippingOrderRequest.orderId) ?: throw OrderNotFoundException()

        val productIdList = shippingOrderRequest.shippedProductList.map { it.productId }

        val updatedProducts = productOrderRepository.selectToUpdateLock(shippingOrderRequest.orderId, productIdList)

        if(updatedProducts.size != productIdList.size) {
            // DN: Also we can return here list of product ids which was not found when need
            throw ProductNotFoundException()
        }
        // Checking if shipped items is less than total amount
        val updatedProductById = updatedProducts.associateBy { it.productId }
        shippingOrderRequest.shippedProductList.forEach{
            val foundProduct = updatedProductById[it.productId]
            if(foundProduct == null) {
                throw IllegalStateException("As far as updated products equal all product id list - updated product id should be found")
            }
            val totalShippedNewState = foundProduct.productsAmountShipped + it.amount
            if(totalShippedNewState > foundProduct.productsAmountInOrder) {
                throw TotalShippedIsAboveThanInOrder()
            }
        }

        productOrderRepository.batchUpdateProductsAmountShipped(shippingOrderRequest.orderId, shippingOrderRequest.shippedProductList)

        val isOrderShipped = productOrderRepository.isAllProductsInOrderAreShipped(shippingOrderRequest.orderId)

        return if(isOrderShipped) {
            notificationRepository.createNotification("Order " + shippingOrderRequest.orderId + " is shipped. There is your Money!", order.merchantId )
            val newStatus = OrderStatus.SHIPPED
            orderRepository.updateOrderStatus(order.id, newStatus)
            newStatus
        } else {
            order.status
        }
    }


}