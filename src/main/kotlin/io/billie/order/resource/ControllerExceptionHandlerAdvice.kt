package io.billie.order.resource

import io.billie.order.exception.OrganisationNotFoundException
import io.billie.order.exception.SimpleBusinessMessageException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ControllerExceptionHandlerAdvice {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(SimpleBusinessMessageException::class)
    fun handleSimpleBusinessMessageException(ex: OrganisationNotFoundException): ResponseEntity<String> {
        // DN: Actually there can be response structure with codes, but it's need to also change organisation response structure
        return ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<String> {
        log.error("Uncaught internal exception", ex)
        return ResponseEntity("Internal Server Error: ", HttpStatus.INTERNAL_SERVER_ERROR)
    }
}