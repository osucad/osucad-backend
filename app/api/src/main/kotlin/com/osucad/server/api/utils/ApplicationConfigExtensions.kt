package com.osucad.server.api.utils

import io.ktor.server.config.ApplicationConfig
import kotlin.reflect.KProperty

operator fun ApplicationConfig.getValue(thisRef: Any?, property: KProperty<*>): String {
    return this.property(property.name).getString()
}
