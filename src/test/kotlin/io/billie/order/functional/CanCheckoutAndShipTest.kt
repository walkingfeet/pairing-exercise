package io.billie.order.functional

import com.fasterxml.jackson.databind.ObjectMapper
import io.billie.SpringIntegrationTest
import io.billie.order.data.OrderRepository
import io.billie.order.data.ProductOrderRepository
import io.billie.order.model.Order
import io.billie.order.model.OrderStatus
import io.billie.order.model.ProductOrder
import io.billie.order.viewmodel.CheckoutOrderRequest
import io.billie.order.viewmodel.CreateProductRequest
import io.billie.order.viewmodel.OrderStatusEntity
import io.billie.order.viewmodel.ProductInOrder
import io.billie.order.viewmodel.ShippedProduct
import io.billie.order.viewmodel.ShippingOrderRequest
import io.billie.organisations.functional.data.Fixtures
import io.billie.organisations.viewmodel.Entity
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CanCheckoutAndShipTest: SpringIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var productOrderRepository: ProductOrderRepository

    private val totalPriceTemplate = BigDecimal("121.00")

    private val productAmountTemplate = 5

    @Test
    fun `should return bad request when not nullable keys are null in creating account`() {
        // Make json for setting nulls in not nullable object
        val orderNullOrgIds = """{
              "buyerId": null,
              "merchantId": null,
              "totalPrice": 1,
              "productList": [
                {
                  "productId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "amount": 1
                }
              ]
            }         
        """.trimIndent()

       mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON).content(orderNullOrgIds)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }


    @Test
    fun `should return bad request when empty list of product in creating order`() {
        // Create orgs for correct ids
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()

        val order = CheckoutOrderRequest(buyerId = buyerEntity.id, merchantId = merchantEntity.id, totalPrice = totalPriceTemplate, emptyList())

        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(order))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return bad request when total amount is zero list of product in creating order`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)
        val productList = arrayListOf(ProductInOrder(productId = productEntityId.id, 22))

        val order = CheckoutOrderRequest(buyerId = buyerEntity.id, merchantId = merchantEntity.id, totalPrice = BigDecimal.ZERO, productList)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(order))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return bad request when total amount of product is negative in list of product in creating order`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)
        val productList = arrayListOf(ProductInOrder(productId = productEntityId.id, -3))

        val order = CheckoutOrderRequest(buyerId = buyerEntity.id, merchantId = merchantEntity.id, totalPrice = totalPriceTemplate, productList)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(order))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should create an order`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)
        val productList = arrayListOf(ProductInOrder(productId = productEntityId.id, productAmountTemplate))

        val orderCheckoutRequest = CheckoutOrderRequest(buyerId = buyerEntity.id, merchantId = merchantEntity.id, totalPrice = totalPriceTemplate, productList)

        val orderEntity = mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(orderCheckoutRequest))
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn()

        val orderIdEntity = mapper.readValue(orderEntity.response.contentAsString, Entity::class.java)

        val orderDatabase = orderRepository.findOrderById(orderId = orderIdEntity.id)
        assertNotNull(orderDatabase, "Order should be created")
        val expectedOrder = Order(orderDatabase.id, buyerEntity.id, merchantEntity.id, totalPriceTemplate, OrderStatus.IN_PROGRESS, orderDatabase.created, orderDatabase.updated)

        assertEquals(expectedOrder, orderDatabase)

        val products = productOrderRepository.findProductOrdersByOrderId(orderIdEntity.id)
        assertTrue(products.size == 1, "Should create only one product in order")
        val actualProductInOrder = products[0]

        val expectedProductInOrder = ProductOrder(actualProductInOrder.id, orderIdEntity.id, productEntityId.id, productAmountTemplate, 0, actualProductInOrder.created, actualProductInOrder.updated)

        assertEquals(expectedProductInOrder, actualProductInOrder)
    }

    @Test
    fun `should return bad request when buyer not exists`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)
        val productList = arrayListOf(ProductInOrder(productId = productEntityId.id, productAmountTemplate))

        val orderCheckoutRequest = CheckoutOrderRequest(buyerId = UUID.randomUUID(), merchantId = merchantEntity.id, totalPrice = totalPriceTemplate, productList)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(orderCheckoutRequest))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return bad request when merchant not exists`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)
        val productList = arrayListOf(ProductInOrder(productId = productEntityId.id, productAmountTemplate))

        val orderCheckoutRequest = CheckoutOrderRequest(buyerId = buyerEntity.id, merchantId = UUID.randomUUID(), totalPrice = totalPriceTemplate, productList)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(orderCheckoutRequest))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return bad request when product not exists`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productList = arrayListOf(ProductInOrder(productId = UUID.randomUUID(), productAmountTemplate))

        val orderCheckoutRequest = CheckoutOrderRequest(buyerId = buyerEntity.id, merchantId = merchantEntity.id, totalPrice = totalPriceTemplate, productList)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(orderCheckoutRequest))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return bad request when product not merchants`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(buyerEntity)

        val productList = arrayListOf(ProductInOrder(productId = productEntityId.id, productAmountTemplate))

        val orderCheckoutRequest = CheckoutOrderRequest(buyerId = buyerEntity.id, merchantId = merchantEntity.id, totalPrice = totalPriceTemplate, productList)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(orderCheckoutRequest))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }



    @Test
    fun `should return bad request when shipment is empty`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)
        val orderEntityId = createOrderWithBuyerMerchantAndProductId(buyerEntity, merchantEntity, productEntityId)

        val shipOrder = ShippingOrderRequest(orderEntityId.id, shippedProductList = emptyList())

        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/shipment")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(shipOrder))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return bad request when shipment is zero`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)
        val orderEntityId = createOrderWithBuyerMerchantAndProductId(buyerEntity, merchantEntity, productEntityId)

        val shipOrder = ShippingOrderRequest(orderEntityId.id, shippedProductList = arrayListOf(ShippedProduct(productEntityId.id, 0)))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/shipment")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(shipOrder))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return bad request when order not found`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)
        val orderEntityId = createOrderWithBuyerMerchantAndProductId(buyerEntity, merchantEntity, productEntityId)

        val shipOrder = ShippingOrderRequest(UUID.randomUUID(), shippedProductList = arrayListOf(ShippedProduct(productEntityId.id, 3)))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/shipment")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(shipOrder))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should create and ship an order when product in order not found`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)
        val productNotInOrderId = createProductForMerchant(merchantEntity)

        val orderEntityId = createOrderWithBuyerMerchantAndProductId(buyerEntity, merchantEntity, productEntityId)

        val shipOrder = ShippingOrderRequest(orderEntityId.id, shippedProductList = arrayListOf(ShippedProduct(productNotInOrderId.id, 3)))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/shipment")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(shipOrder))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return bad request when amount of shipped order is above than exists`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)
        val productNotInOrderId = createProductForMerchant(merchantEntity)

        val orderEntityId = createOrderWithBuyerMerchantAndProductId(buyerEntity, merchantEntity, productEntityId)

        val shipOrder = ShippingOrderRequest(orderEntityId.id, shippedProductList = arrayListOf(ShippedProduct(productNotInOrderId.id, productAmountTemplate + 1)))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/shipment")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(shipOrder))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `should return in_progress order status when order is not shipped`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)

        val orderEntityId = createOrderWithBuyerMerchantAndProductId(buyerEntity, merchantEntity, productEntityId)

        val shipOrder = ShippingOrderRequest(orderEntityId.id, shippedProductList = arrayListOf(ShippedProduct(productEntityId.id, productAmountTemplate - 1)))

        val orderStatusResponse = mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/shipment")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(shipOrder))
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn()

        val orderStatusEntity = mapper.readValue(orderStatusResponse.response.contentAsString, OrderStatusEntity::class.java)

        assertEquals(OrderStatusEntity(OrderStatus.IN_PROGRESS), orderStatusEntity)
    }

    @Test
    fun `should return shipped order status when order is shipped`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)

        val orderEntityId = createOrderWithBuyerMerchantAndProductId(buyerEntity, merchantEntity, productEntityId)

        val shipOrder = ShippingOrderRequest(orderEntityId.id, shippedProductList = arrayListOf(ShippedProduct(productEntityId.id, productAmountTemplate)))

        val orderStatusResponse = mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/shipment")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(shipOrder))
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn()

        val orderStatusEntity = mapper.readValue(orderStatusResponse.response.contentAsString, OrderStatusEntity::class.java)

        assertEquals(OrderStatusEntity(OrderStatus.SHIPPED), orderStatusEntity)
    }

    @Test
    fun `should return first in_progress then order is shipped when shippment is splitted`() {
        val (buyerEntity, merchantEntity) = createBuyerAndMerchant()
        val productEntityId = createProductForMerchant(merchantEntity)

        val orderEntityId = createOrderWithBuyerMerchantAndProductId(buyerEntity, merchantEntity, productEntityId)

        val shipOrder = ShippingOrderRequest(orderEntityId.id, shippedProductList = arrayListOf(ShippedProduct(productEntityId.id, productAmountTemplate - 1)))

        val orderStatusResponse = mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/shipment")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(shipOrder))
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn()

        val orderStatusEntity = mapper.readValue(orderStatusResponse.response.contentAsString, OrderStatusEntity::class.java)

        assertEquals(OrderStatusEntity(OrderStatus.IN_PROGRESS), orderStatusEntity)
        // Second shipment

        val shipOrderSecond = ShippingOrderRequest(orderEntityId.id, shippedProductList = arrayListOf(ShippedProduct(productEntityId.id, 1)))

        val orderStatusResponseAfterSecondShipment = mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/shipment")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(shipOrderSecond))
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn()

        val orderStatusEntityAfterSecondShipment = mapper.readValue(orderStatusResponseAfterSecondShipment.response.contentAsString, OrderStatusEntity::class.java)

        assertEquals(OrderStatusEntity(OrderStatus.SHIPPED), orderStatusEntityAfterSecondShipment)

    }

    private fun createBuyerAndMerchant(): Pair<Entity, Entity> {
        val buyerCreateResponse = mockMvc.perform(
            MockMvcRequestBuilders.post("/organisations")
                .contentType(MediaType.APPLICATION_JSON).content(Fixtures.orgRequestJson())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val buyerEntity = mapper.readValue(buyerCreateResponse.response.contentAsString, Entity::class.java)

        // Create orgs for correct ids
        val merchantCreateResponse = mockMvc.perform(
            MockMvcRequestBuilders.post("/organisations")
                .contentType(MediaType.APPLICATION_JSON).content(Fixtures.orgRequestJson())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val merchantEntity = mapper.readValue(merchantCreateResponse.response.contentAsString, Entity::class.java)
        return Pair(buyerEntity, merchantEntity)
    }

    private fun createProductForMerchant(merchantEntityId: Entity): Entity {
        val product = CreateProductRequest("Simple product", merchantEntityId.id)

        val productJsonResponse = mockMvc.perform(
            MockMvcRequestBuilders.post("/products")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(product))
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn()

        return mapper.readValue(productJsonResponse.response.contentAsString, Entity::class.java)
    }

    private fun createOrderWithBuyerMerchantAndProductId(buyerEntityId: Entity, merchantEntityId: Entity, productEntityId: Entity) : Entity {

        val productList = arrayListOf(ProductInOrder(productId = productEntityId.id, productAmountTemplate))

        val orderCheckoutRequest = CheckoutOrderRequest(buyerId = buyerEntityId.id, merchantId = merchantEntityId.id, totalPrice = totalPriceTemplate, productList)


        val orderEntity = mockMvc.perform(
            MockMvcRequestBuilders.post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(orderCheckoutRequest))
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn()

        return mapper.readValue(orderEntity.response.contentAsString, Entity::class.java)
    }

}