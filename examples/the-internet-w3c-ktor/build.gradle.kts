plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "me.darkweird.sekt.examples"
version = "0.1.0"


repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
    implementation(project(":w3c"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":examples:common"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.ktor.client.cio)
}


tasks.test {
    useJUnitPlatform()
}
