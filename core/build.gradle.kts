val ktor_version: String by project
val kotlin_version: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "me.darkweird.sekt"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-cio-jvm:$ktor_version")

    testImplementation("io.kotest:kotest-runner-junit5:4.6.2")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktor_version")
}