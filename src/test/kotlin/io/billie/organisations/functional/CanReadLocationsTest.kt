package io.billie.organisations.functional

import io.billie.SpringIntegrationTest
import io.billie.organisations.functional.matcher.IsUUID.isUuid
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CanReadLocationsTest: SpringIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun notFoundForUnknownCountry() {
        mockMvc.perform(
            get("/countries/xx/cities")
                .contentType(APPLICATION_JSON)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun canViewCountries() {
        mockMvc.perform(
            get("/countries")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.[0].name").value("Andorra"))
            .andExpect(jsonPath("$.[0].id").value(isUuid()))
            .andExpect(jsonPath("$.[0].country_code").value("AD"))
            .andExpect(jsonPath("$.[239].name").value("Zimbabwe"))
            .andExpect(jsonPath("$.[239].id").value(isUuid()))
            .andExpect(jsonPath("$.[239].country_code").value("ZW"))
    }

}
