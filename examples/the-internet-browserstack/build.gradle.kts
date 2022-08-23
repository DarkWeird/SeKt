val ktor_version: String by project
val kotlin_version: String by project

plugins {
    kotlin("jvm") // version provides by root project
    kotlin("plugin.serialization")
}

group = "me.darkweird.sekt.examples"
version = "0.1.0"


repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":core"))
    implementation(project(":browserstack"))
    implementation(project(":w3c"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    testImplementation("io.kotest:kotest-runner-junit5:4.6.2")
    testImplementation("ch.qos.logback:logback-classic:1.2.10")
    testImplementation("io.ktor:ktor-client-cio-jvm:$ktor_version")
    testImplementation("io.ktor:ktor-client-logging-jvm:$ktor_version")
}


tasks.test {
    useJUnitPlatform()
}
