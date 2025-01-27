package com.osucad.server.api.config

import com.osucad.server.api.repositories.RepositoryModule
import com.osucad.server.api.services.ServiceModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {
    install(Koin) {
        modules(
            module {
                single<ApplicationConfig> { environment.config }
            },
            ConfigModule().module,
            RepositoryModule().module,
            ServiceModule().module
        )
    }
}