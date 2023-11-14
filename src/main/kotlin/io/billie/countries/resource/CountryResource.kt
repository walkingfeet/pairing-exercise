package io.billie.countries.resource

import io.billie.countries.model.CountryResponse
import io.billie.countries.service.CountryService
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("countries")
class CountryResource(val service: CountryService) {

    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "All countries",
                content = [
                    (Content(
                        mediaType = "application/json",
                        array = (ArraySchema(schema = Schema(implementation = CountryResponse::class)))
                    ))]
            )]
    )
    @GetMapping
    fun index(): List<CountryResponse> = service.findCountries()

}
