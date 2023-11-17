package io.billie.organisations.data

import io.billie.order.exception.SimpleBusinessMessageException

class UnableToFindCountry(val countryCode: String) : SimpleBusinessMessageException("Could not find country $countryCode")
