package io.billie.order.data

import io.billie.order.model.NotificationStatus
import io.billie.order.model.OrderNotification
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

@Repository
class OrderNotificationRepository(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    fun createNotification(notificationDescription: String, organisationId: UUID) : UUID {
        val sql = """INSERT INTO orders_schema.order_notifications (notification_description, organisation_id, status) 
                VALUES (:notificationDescription, :organisationId, :status)""".trimIndent()

        val keyHolder = GeneratedKeyHolder()

        val params = MapSqlParameterSource()
            .addValue("notificationDescription", notificationDescription)
            .addValue( "organisationId", organisationId)
            .addValue("status", NotificationStatus.NEW.toString())


         namedParameterJdbcTemplate.update(sql,params ,keyHolder, arrayOf("id"))

        return keyHolder.getKeyAs(UUID::class.java)!!
    }

    fun findById(id: UUID): OrderNotification? {
        val sql = "SELECT id, notification_description, organisation_id, status FROM orders_schema.order_notifications WHERE id = :id"
        val params =  mapOf("id" to id)
        return try{
            namedParameterJdbcTemplate.queryForObject(
                sql,
                params,
                OrderNotificationsRowMapper()
            )
        } catch (ex: EmptyResultDataAccessException) {
            return null
        }
    }

    private class OrderNotificationsRowMapper : RowMapper<OrderNotification> {
        override fun mapRow(rs: ResultSet, rowNum: Int): OrderNotification {
            return OrderNotification(
                id = rs.getObject("id", UUID::class.java),
                notificationDescription = rs.getString("notification_description"),
                organisationId = rs.getObject("organisation_id", UUID::class.java),
                // DN: There is also option to use simple numbers mapping like 1 -> NEW, for simplicity left like this
                status = NotificationStatus.valueOf(rs.getString("status"))
            )
        }
    }
}