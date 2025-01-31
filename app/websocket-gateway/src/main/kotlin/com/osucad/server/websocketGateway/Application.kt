package com.osucad.server.websocketGateway

import com.osucad.server.multiplayer.local.LocalGateway
import com.osucad.server.multiplayer.redis.RedisGateway
import com.osucad.server.multiplayer.types.SequenceNumber
import com.osucad.server.multiplayer.types.SummaryMessage
import com.osucad.server.multiplayer.types.UserInfo
import com.osucad.server.multiplayer.redis.RedisMessageBroadcaster
import com.osucad.server.websocketGateway.plugins.configureMetrics
import com.osucad.server.websocketGateway.plugins.configureWebSockets
import io.ktor.server.application.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
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

    val redis = createRedis(environment.config)

    val broadcaster = RedisMessageBroadcaster(redis, meterRegistry)

    val gateway = RedisGateway(redis, meterRegistry)


    GlobalScope.launch(Job()) {
        broadcaster.beginPolling()
    }

    // TODO: get room id from request
    val roomId = UUID.randomUUID()

    redis.getBucket<SummaryMessage>("osucad:edit:$roomId:summary").set(SummaryMessage(0, SequenceNumber(0), ""))

    routing {
        get {
            call.respondText("Hello, world!")
        }

        webSocket {
            val socket = KtorWebSocketAdapter(this)

            // TODO: use token for this
            val user = UserInfo(0, "guest")

            gateway.accept(socket, roomId, user)
        }
    }
}