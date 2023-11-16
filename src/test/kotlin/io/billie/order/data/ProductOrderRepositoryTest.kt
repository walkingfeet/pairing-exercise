package io.billie.order.data

import io.billie.SpringIntegrationTest
import io.billie.order.model.OrderStatus
import io.billie.order.model.ProductOrder
import io.billie.order.viewmodel.ProductInOrder
import io.billie.order.viewmodel.ProductRequest
import io.billie.order.viewmodel.ProductShipment
import io.billie.organisations.data.OrganisationRepository
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductOrderRepositoryTest: SpringIntegrationTest() {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Autowired
    private lateinit var productOrderRepository: ProductOrderRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var organisationRepository: OrganisationRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var transactionalHelper: TransactionalHelper

    @Test
    fun `should create and find product order list`() {
        val (merchantOrgId, orderId) = createOrder()

        val (productOneId, productTwoId) = createTwoProducts(merchantOrgId)

        val createdListOfProducts = listOf(ProductInOrder(productOneId, 3), ProductInOrder(productTwoId, 5))
        productOrderRepository.createOrderProducts(orderId, createdListOfProducts)

        val result = productOrderRepository.findProductOrdersByOrderId(orderId)
        val expected = listOf(
            ProductOrder(
                id = result[0].id,
                orderId = orderId,
                productId = createdListOfProducts[0].productId,
                productsAmountInOrder = createdListOfProducts[0].amount,
                productsAmountShipped = 0,
                created = result[0].created,
                updated = result[0].updated
            ),
            ProductOrder(
                id = result[1].id,
                orderId = orderId,
                productId = createdListOfProducts[1].productId,
                productsAmountInOrder = createdListOfProducts[1].amount,
                productsAmountShipped = 0,
                created = result[1].created,
                updated = result[1].updated
            )
        )

        assertEquals(expected, result)
    }

    private fun createTwoProducts(merchantOrgId: UUID): Pair<UUID, UUID> {
        val productOneId = productRepository.createProduct(ProductRequest("first", merchantOrgId))
        val productTwoId = productRepository.createProduct(ProductRequest("second", merchantOrgId))
        return Pair(productOneId, productTwoId)
    }

    private fun createOrder(): Pair<UUID, UUID> {
        val buyerOrgId = organisationRepository.create(DataTemplates.organisationRequestTemplate)
        val merchantOrgId = organisationRepository.create(DataTemplates.organisationRequestTemplate)
        val orderId = orderRepository.createOrder(buyerOrgId, merchantOrgId, BigDecimal.ONE, OrderStatus.IN_PROGRESS)
        return Pair(merchantOrgId, orderId)
    }

    @Test
    fun `should create and update products that shipped`() {
        val (merchantOrgId, orderId) = createOrder()

        val (productOneId, productTwoId) = createTwoProducts(merchantOrgId)

        val createdListOfProducts = listOf(ProductInOrder(productOneId, 4), ProductInOrder(productTwoId, 6))
        productOrderRepository.createOrderProducts(orderId, createdListOfProducts)

        val productUpdates = listOf(ProductShipment(productOneId, 2), ProductShipment(productTwoId, 3))
        productOrderRepository.batchUpdateProductsAmountShipped(orderId, productUpdates)


        val result = productOrderRepository.findProductOrdersByOrderId(orderId)
        val expected = listOf(
            ProductOrder(
                id = result[0].id,
                orderId = orderId,
                productId = createdListOfProducts[0].productId,
                productsAmountInOrder = createdListOfProducts[0].amount,
                productsAmountShipped = productUpdates[0].additionalAmountShipped,
                created = result[0].created,
                updated = result[0].updated
            ),
            ProductOrder(
                id = result[1].id,
                orderId = orderId,
                productId = createdListOfProducts[1].productId,
                productsAmountInOrder = createdListOfProducts[1].amount,
                productsAmountShipped = productUpdates[1].additionalAmountShipped,
                created = result[1].created,
                updated = result[1].updated
            )
        )

        assertEquals(expected, result)

        val shipped = productOrderRepository.isAllProductsInOrderAreShipped(orderId)

        assertFalse(shipped, "Product order must not be shipped")
    }

    @Test
    fun `should create and update products and got shipped when all goods in the orders are shipped`() {
        val (merchantOrgId, orderId) = createOrder()

        val (productOneId, productTwoId) = createTwoProducts(merchantOrgId)

        val createdListOfProducts = listOf(ProductInOrder(productOneId, 4), ProductInOrder(productTwoId, 6))
        productOrderRepository.createOrderProducts(orderId, createdListOfProducts)

        val productUpdatesFirst = listOf(ProductShipment(productOneId, 3), ProductShipment(productTwoId, 5))
        productOrderRepository.batchUpdateProductsAmountShipped(orderId, productUpdatesFirst)
        val productUpdatesSecond = listOf(ProductShipment(productOneId, 1), ProductShipment(productTwoId, 1))
        productOrderRepository.batchUpdateProductsAmountShipped(orderId, productUpdatesSecond)

        val shipped = productOrderRepository.isAllProductsInOrderAreShipped(orderId)

        assertTrue(shipped, "Order must be shipped")
    }

    @Test
    fun `should test select for update`() {
        val (merchantOrgId, orderId) = createOrder()

        val (productOneId, productTwoId) = createTwoProducts(merchantOrgId)

        val createdListOfProducts = listOf(ProductInOrder(productOneId, 4), ProductInOrder(productTwoId, 6))
        productOrderRepository.createOrderProducts(orderId, createdListOfProducts)
        // Starting in parallel
        val latch = CountDownLatch(1)
        val mainTransactionFinished = "MAIN_FINISHED"
        val updateFinished = "UPDATE_FINISHED"

        val actionOrderLog = LinkedBlockingQueue<String>()
        thread {
            transactionalHelper.executeInTransaction {
                log.debug("Waiting for main transaction update")
                latch.await() // Wait for the main transaction to acquire the lock
                log.debug("Started update of product")
                val productUpdatesFirst = listOf(ProductShipment(productOneId, 3), ProductShipment(productTwoId, 5))
                productOrderRepository.batchUpdateProductsAmountShipped(orderId, productUpdatesFirst)
                log.debug("Finished update of products")
                actionOrderLog.add(updateFinished)
            }
        }
        transactionalHelper.executeInTransaction {
            log.debug("Make selectForUpdate lock")

            productOrderRepository.selectToUpdateLock(orderId, listOf(productOneId, productTwoId))
            log.debug("Unlock the update thread and sleep to make update stuck")
            latch.countDown()
            // Just sleep  - then we can be sure that productOrderRepository locks in external thread
            // Alternative - is timeout for transaction and check exception. but we also should sleep until timeout - no difference
            Thread.sleep(1000)
            log.debug("Finished select for update")
            actionOrderLog.add(mainTransactionFinished)
        }

        val firstEvent = actionOrderLog.take()
        assertEquals(mainTransactionFinished, firstEvent)
        val secondEvent = actionOrderLog.take()
        assertEquals(updateFinished, secondEvent)
    }

}