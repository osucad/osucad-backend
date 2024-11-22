plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.ktor)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(project(":lib:multiplayer"))
    implementation(project(":lib:ktor-redis"))

    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.bundles.lettuce)
    implementation(libs.bundles.ktor)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.testHost)
}
