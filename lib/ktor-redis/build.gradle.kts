plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(libs.bundles.lettuce)
    implementation(libs.bundles.ktor.server)
}
