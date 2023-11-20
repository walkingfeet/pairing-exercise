package io.billie.order.functional

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc

class CanCheckoutAndShipTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var template: JdbcTemplate
//
//    @Test
//    fun `should create and ship an order`() {
//        TODO()
//    }
//
//    @Test
//    fun `should return bad request when cannot find an organisation`() {
//        TODO()
//    }
//
//    @Test
//    fun `should return bad request when cannot find an order during shipment`() {
//        TODO()
//    }
//
//    @Test
//    fun `should return bad request when cannot find a product during checkout `() {
//        TODO()
//    }
//
//    @Test
//    fun `should return bad request when amount of product is above total in order during shipment`() {
//        TODO()
//    }
}