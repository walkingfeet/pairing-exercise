package io.billie.order.service

import io.billie.order.data.ProductRepository
import io.billie.order.exception.OrganisationNotFoundException
import io.billie.order.viewmodel.CreateProductRequest
import io.billie.organisations.data.OrganisationRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProductService(private val productRepository: ProductRepository, private val organisationRepository: OrganisationRepository) {

    fun createProduct(createProductRequest: CreateProductRequest): UUID {
        // Check that organisation exists
        organisationRepository.findOrganisationById(createProductRequest.organisationId)
            ?: throw OrganisationNotFoundException()

        return productRepository.createProduct(createProductRequest)
    }
}