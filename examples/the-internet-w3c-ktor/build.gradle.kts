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
    implementation(project(":w3c"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    testImplementation("io.kotest:kotest-runner-junit5:4.6.2")
    testImplementation("io.ktor:ktor-client-cio:$ktor_version")
}


tasks.test {
    useJUnitPlatform()
}
