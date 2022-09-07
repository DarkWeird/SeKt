package me.darkweird.sekt.core

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder

class WebDriverBuilder {
    var httpClientProvider: (() -> HttpClient) = { HttpClient(CIO) { httpClientConfig.forEach { this.apply(it) } } }
    var httpClientConfig = mutableListOf<HttpClientConfig<*>.() -> Unit>({ defaultSettings() })
    var jsonProvider: (() -> Json) = { Json }
    val webDriverConfig = WebDriverConfig()

    fun <T : HttpClientEngineConfig> ktor(
        engine: HttpClientEngineFactory<T>,
        config: HttpClientConfig<T>.() -> Unit = {}
    ) {
        httpClientProvider = {
            HttpClient(engine) {
                httpClientConfig.forEach { this.apply(it) }
            }
        }
        httpClientConfig.add(config as HttpClientConfig<*>.() -> Unit)
    }

    fun ktor(
        config: HttpClientConfig<*>.() -> Unit = {}
    ) {
        httpClientConfig.add(config)
    }

    fun json(builder: JsonBuilder.() -> Unit) {
        jsonProvider = { Json { builder() } }
    }

    fun webdriver(builder: WebDriverConfig.() -> Unit) {
        builder(webDriverConfig)
    }
}

class WebDriverConfig {
    val errorConverters = mutableListOf(defaultConverter())


    fun addErrorConverter(errorConverter: ErrorConverter) {
        errorConverters.add(errorConverter)
    }

    fun addErrorConverters(errorConverters: List<ErrorConverter>) {
        this.errorConverters.addAll(errorConverters)
    }
}


private fun <T : HttpClientEngineConfig> HttpClientConfig<T>.defaultSettings() {
    install(UserAgent) {
        this.agent = "(Se)lenium(K)ol(t)in client"
    }

    expectSuccess = false
    install(ContentNegotiation) {
        json(Json)
    }

    install(DefaultRequest) {
        header(HttpHeaders.Connection, "keep-alive")
        header("Keep-Alive", "timeout=5, max=1000")
    }
}
