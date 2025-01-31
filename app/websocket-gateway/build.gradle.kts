plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.ktor)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=false")
}

dependencies {
    api(project(":lib:multiplayer"))

    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.bundles.ktor.server)
    implementation(libs.redisson)

    implementation(libs.micrometer.registry.prometheus)
}
