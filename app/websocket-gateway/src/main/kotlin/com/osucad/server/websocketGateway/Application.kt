package com.osucad.server.websocketGateway

import com.osucad.server.multiplayer.redis.RedisGateway
import com.osucad.server.multiplayer.types.SequenceNumber
import com.osucad.server.multiplayer.types.SummaryMessage
import com.osucad.server.multiplayer.types.UserInfo
import com.osucad.server.multiplayer.redis.RedisMessageBroadcaster
import com.osucad.server.websocketGateway.plugins.configureMetrics
import com.osucad.server.websocketGateway.plugins.configureWebSockets
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}


@OptIn(DelicateCoroutinesApi::class)
fun Application.module() {
    val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    configureMetrics(meterRegistry)
    configureWebSockets()

    install(CORS) {
        anyHost()
        anyMethod()
    }

    val redis = createRedis(environment.config)

    val broadcaster = RedisMessageBroadcaster(redis, meterRegistry)

    val gateway = RedisGateway(redis, meterRegistry)


    GlobalScope.launch(Job()) {
        broadcaster.beginPolling()
    }

    val jwtVerifier = createJwtVerifier()

    routing {
        webSocket("/api/multiplayer") {
            val tokenString = call.request.queryParameters["token"]

            if (tokenString == null) {
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Missing token"))
                return@webSocket
            }

            val token = runCatching {
                jwtVerifier.verify(tokenString)
            }.getOrElse {
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Invalid token"))
                return@webSocket
            }

            val socket = KtorWebSocketAdapter(this)

            val roomId = UUID.fromString(token.getClaim("roomId").asString())

            val user = UserInfo(
                id = token.subject.toInt(),
                username = token.getClaim("name").asString(),
            )

            gateway.accept(socket, roomId, user)
        }
    }
}