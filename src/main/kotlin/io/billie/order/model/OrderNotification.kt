package io.billie.order.model

import java.util.UUID

data class OrderNotification(
    val id: UUID,
    val notificationDescription: String,
    val organisationId: UUID,
    val status: NotificationStatus
)