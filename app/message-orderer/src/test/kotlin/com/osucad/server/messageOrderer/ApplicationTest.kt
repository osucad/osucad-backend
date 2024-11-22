package com.osucad.server.messageOrderer

import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldHave
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.config.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import org.testcontainers.containers.GenericContainer
import kotlin.time.Duration.Companion.seconds

class ApplicationTest : FunSpec({
    val redis = install(ContainerExtension(GenericContainer("redis:7.4.1-alpine"))) {
        startupAttempts = 1
        withExposedPorts(6379)
    }

    test("Application becomes ready on startup") {
        testApplication {
            environment {
                config = MapApplicationConfig(
                    "osucad.redis.hostname" to redis.host,
                    "osucad.redis.port" to "6379"
                )
            }

            application {
                module()
            }

            val client = createClient {}

            eventually(30.seconds) {
                val response = client.get("/ready")

                response shouldHaveStatus 200
                response.bodyAsText() shouldBe """{"redis":true}"""
            }
        }
    }
})