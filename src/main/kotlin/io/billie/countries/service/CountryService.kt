package io.billie.countries.service

import io.billie.countries.data.CountryRepository
import io.billie.countries.model.CountryResponse
import org.springframework.stereotype.Service

@Service
class CountryService(val dbCountry: CountryRepository) {

    fun findCountries(): List<CountryResponse> {
        return dbCountry.findCountries()
    }
}
