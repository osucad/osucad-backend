package com.osucad.server.api.exceptions

import io.ktor.http.HttpStatusCode

open class ResponseStatusException(val status: HttpStatusCode, message: String? = null) : Exception(message)

class BadRequestException(message: String? = null) : ResponseStatusException(HttpStatusCode.BadRequest, message)

class UnauthorizedException(message: String? = null) : ResponseStatusException(HttpStatusCode.Unauthorized, message)

class ForbiddenException(message: String? = null) : ResponseStatusException(HttpStatusCode.Forbidden, message)

class NotFoundException(message: String? = null) : ResponseStatusException(HttpStatusCode.NotFound, message)

class MethodNotAllowedException(message: String? = null) :
    ResponseStatusException(HttpStatusCode.MethodNotAllowed, message)

class InternalServerErrorException(message: String? = null) :
    ResponseStatusException(HttpStatusCode.InternalServerError, message)

class BadGatewayException(message: String? = null) : ResponseStatusException(HttpStatusCode.BadGateway, message)

class ServiceUnavailableException(message: String? = null) :
    ResponseStatusException(HttpStatusCode.ServiceUnavailable, message)

class GatewayTimeoutException(message: String? = null) : ResponseStatusException(HttpStatusCode.GatewayTimeout, message)

class VersionNotSupportedException(message: String? = null) :
    ResponseStatusException(HttpStatusCode.VersionNotSupported, message)
