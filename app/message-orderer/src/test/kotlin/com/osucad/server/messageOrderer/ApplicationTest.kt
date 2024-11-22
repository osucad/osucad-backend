package com.osucad.server.messageOrderer

import com.redis.testcontainers.RedisContainer
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import kotlin.time.Duration.Companion.seconds

class ApplicationTest : FunSpec({
    val redis = RedisContainer("redis:7.4.1-alpine")

    redis.start()

    test("Application becomes ready on startup") {
        testApplication {
            environment {
                config = RedisApplicationConfig(redis)
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