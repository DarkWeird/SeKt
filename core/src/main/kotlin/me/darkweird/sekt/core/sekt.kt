package me.darkweird.sekt.core

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder


fun <T : HttpClientEngineConfig> webdriver(
    baseUrl: String,
    ktorEngine: HttpClientEngineFactory<T>,
    httpConfig: HttpClientConfig<T>.() -> Unit = {},
    jsonConfig: JsonBuilder.() -> Unit = {}
): WebDriver = webdriver(baseUrl, listOf(), ktorEngine, httpConfig, jsonConfig)

fun <T : HttpClientEngineConfig> webdriver(
    baseUrl: String,
    errorConverters: List<ErrorConverter>,
    ktorEngine: HttpClientEngineFactory<T>,
    httpConfig: HttpClientConfig<T>.() -> Unit = {},
    jsonConfig: JsonBuilder.() -> Unit = {}
): WebDriver =
    WebDriver(baseUrl) {
        WebDriverConfig(errorConverters,
            Json { jsonConfig(this) }) {
            HttpClient(
                ktorEngine,
                config(
                    httpConfig
                )
            )
        }
    }

fun webdriver(
    baseUrl: String,
    errorConverters: List<ErrorConverter> = listOf(),
    httpConfig: HttpClientConfig<HttpClientEngineConfig>.() -> Unit = {},
    jsonConfig: JsonBuilder.() -> Unit = {},
): WebDriver =
    WebDriver(baseUrl) {
        WebDriverConfig(errorConverters,
            Json { jsonConfig(this) }) {
            HttpClient {
                config(
                    httpConfig
                )(this as HttpClientConfig<HttpClientEngineConfig>)
            }
        }
    }

private fun <T : HttpClientEngineConfig> config(
    httpConfig: HttpClientConfig<T>.() -> Unit,
): HttpClientConfig<T>.() -> Unit = {

    install(ContentNegotiation) {
        json(Json)
    }
    expectSuccess = false

    install(UserAgent) {
        this.agent = "(Se)lenium(K)ol(t)in client"
    }
    install(DefaultRequest) {
        header(HttpHeaders.Connection, "keep-alive")
        header("Keep-Alive", "timeout=5, max=1000")

    }
    httpConfig()
}
