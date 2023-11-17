package io.billie.order.resource

import io.billie.order.service.ProductService
import io.billie.order.viewmodel.CreateProductRequest
import io.billie.organisations.viewmodel.Entity
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/products")
class ProductResource(val productService: ProductService) {

    @PostMapping
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Accepted the new product",
                content = [
                    (Content(
                        mediaType = "application/json",
                        array = (ArraySchema(schema = Schema(implementation = Entity::class)))
                    ))]
            ),
            ApiResponse(responseCode = "400", description = "Bad request", content = [Content()])]
    )
    // DN: There should be authentication and separating api, but it's exercise
    fun createProduct(@RequestBody @Valid createProductRequest: CreateProductRequest): Entity  {
        return Entity(productService.createProduct(createProductRequest))
    }

}