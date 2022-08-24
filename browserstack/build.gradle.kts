val ktor_version: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    api("io.github.darkweird:browserstack.kt:0.3.1")
    implementation("io.ktor:ktor-client-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-serialization-jvm:$ktor_version")
}