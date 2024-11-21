plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.ktor)
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.bundles.lettuce)
}
