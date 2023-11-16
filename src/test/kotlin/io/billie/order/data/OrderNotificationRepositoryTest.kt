package io.billie.order.data

import io.billie.SpringIntegrationTest
import io.billie.order.model.NotificationStatus
import io.billie.order.model.OrderNotification
import io.billie.organisations.data.OrganisationRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals


class OrderNotificationRepositoryTest: SpringIntegrationTest() {

    @Autowired
    private lateinit var orderNotificationRepository: OrderNotificationRepository

    @Autowired
    private lateinit var organisationRepository: OrganisationRepository

    @Test
    fun `should create and find order by id`() {
        val organisationId = organisationRepository.create(DataTemplates.organisationRequestTemplate)

        val notificationText = "Notification text"
        val notificationId = orderNotificationRepository.createNotification(notificationText, organisationId)
        // Should not find the second - as an example - by find all without where
        orderNotificationRepository.createNotification("Not important", organisationId)

        val notification = orderNotificationRepository.findById(notificationId)
        val expected = OrderNotification(notificationId, notificationText, organisationId, NotificationStatus.NEW)
        assertEquals(expected, notification)
    }

}