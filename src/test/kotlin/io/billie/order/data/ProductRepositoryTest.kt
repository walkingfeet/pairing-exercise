package io.billie.order.data

import io.billie.SpringIntegrationTest
import io.billie.order.viewmodel.CreateProductRequest
import io.billie.organisations.data.OrganisationRepository
import io.billie.organisations.viewmodel.ContactDetailsRequest
import io.billie.organisations.viewmodel.LegalEntityType
import io.billie.organisations.viewmodel.OrganisationRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull


class ProductRepositoryTest: SpringIntegrationTest() {

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var organisationRepository: OrganisationRepository

    @Test
    fun shouldCreateListOfProductsForValidOrganisation() {
        val organisationRequest  = OrganisationRequest(name = "Simple name", dateFounded = LocalDate.of(1997, 8, 8), countryCode = "US", VATNumber = null, registrationNumber = null, legalEntityType = LegalEntityType.COOPERATIVE, contactDetails =
        ContactDetailsRequest(null, null, null)
        )

        val organisationId = organisationRepository.create(organisationRequest)

        val createProductRequestFirst = CreateProductRequest("Product name one", organisationId)
        val createProductRequestSecond = CreateProductRequest("Product name second", organisationId)

        val productIdFirst = productRepository.createProduct(createProductRequestFirst)
        val productIdSecond = productRepository.createProduct(createProductRequestSecond)

        val result = productRepository.findProductsByOrganisationId(organisationId)

        assertEquals(result.size, 2, "Product list length should be 2")
        // order is backward
        val expected = arrayListOf(
            result[0].copy(id = productIdSecond, name = createProductRequestSecond.name),
            result[1].copy(id = productIdFirst, name = createProductRequestFirst.name)
        )
        assertEquals(expected, result)
    }

    @Test
    fun shouldReturnNullWhenNoProductFound() {
        val product = productRepository.findProductById(UUID.randomUUID())
        assertNull(product, "Product should not be found")
    }
}