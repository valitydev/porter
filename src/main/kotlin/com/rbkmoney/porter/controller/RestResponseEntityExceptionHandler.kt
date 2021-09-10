package com.rbkmoney.porter.controller

import com.rbkmoney.openapi.notification.model.Error
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [NotificationClientRequestException::class])
    protected fun handleBadRequest(ex: NotificationClientRequestException, request: WebRequest): ResponseEntity<Error> {
        val error = Error().apply {
            status = HttpStatus.BAD_REQUEST.value()
            error = ex.message
        }
        return ResponseEntity(error, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }
}
