plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
    application
}

application {
    mainClass.set("com.osucad.server.messageOrderer.ApplicationKt")
}

dependencies {
    implementation(project(":lib:multiplayer"))

    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.bundles.lettuce)
}
