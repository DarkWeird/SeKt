plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "me.darkweird.sekt"
version = "0.1.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
    implementation(libs.kotlinx.serialization.json)

    api(libs.browserstack.kt)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.serialization)
}