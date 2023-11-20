package io.billie.order.functional

import com.fasterxml.jackson.databind.ObjectMapper
import io.billie.SpringIntegrationTest
import io.billie.order.data.ProductRepository
import io.billie.order.viewmodel.CreateProductRequest
import io.billie.organisations.functional.data.Fixtures
import io.billie.organisations.viewmodel.Entity
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CanCreateProductTest: SpringIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Test
    fun `should create product`() {
        val organisationJsonResponse = mockMvc.perform(
            MockMvcRequestBuilders.post("/organisations")
                .contentType(MediaType.APPLICATION_JSON).content(Fixtures.orgRequestJson())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseOrganisation = mapper.readValue(organisationJsonResponse.response.contentAsString, Entity::class.java)
        val organisationId = responseOrganisation.id

        val product = CreateProductRequest("Simple product", organisationId)

        val productJsonResponse = mockMvc.perform(
            MockMvcRequestBuilders.post("/products")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(product))
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn()

        val productEntityIdResponse = mapper.readValue(productJsonResponse.response.contentAsString, Entity::class.java)
        // Should be found in database
        val foundProduct = productRepository.findProductById(productEntityIdResponse.id)
        assertNotNull(foundProduct, "Product should be found by created id")
        // Other fields are autogenerated by database
        val expected = foundProduct.copy(name = product.name, organisationId = product.organisationId)
        assertEquals(expected, foundProduct)
    }

    @Test
    fun `should return bad request when organisation not found`() {
        val product = CreateProductRequest("Simple product", UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/products")
                .contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(product))
        ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}