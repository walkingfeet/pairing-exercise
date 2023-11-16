package io.billie.order.data

import io.billie.organisations.viewmodel.ContactDetailsRequest
import io.billie.organisations.viewmodel.LegalEntityType
import io.billie.organisations.viewmodel.OrganisationRequest
import java.time.LocalDate

/**
 * Class for getting simple examples for test
 */
class DataTemplates {

    companion object {
        val organisationRequestTemplate  = OrganisationRequest(
            name = "Simple name",
            dateFounded = LocalDate.of(1997, 8, 8),
            countryCode = "US",
            VATNumber = null,
            registrationNumber = "1223",
            legalEntityType = LegalEntityType.COOPERATIVE,
            contactDetails = ContactDetailsRequest(phoneNumber = "123123", fax = "213", email = "example@gmail.com")
        )

    }
}