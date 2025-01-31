package com.osucad.server.websocketGateway

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application

fun Application.createJwtVerifier(): JWTVerifier {
    val config = environment.config.config("osucad.jwt.multiplayer")

    val secret = config.property("secret").getString()
    val issuer = config.property("issuer").getString()
    val audience = config.property("audience").getString()

    return JWT.require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaimPresence("name")
        .withClaimPresence("roomId")
        .withClaimPresence("sub")
        .build()
}