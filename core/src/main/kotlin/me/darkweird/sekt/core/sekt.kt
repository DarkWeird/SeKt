package me.darkweird.sekt.core

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonElement


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
        WebDriverConfig {
            HttpClient(
                ktorEngine,
                config(
                    jsonConfig,
                    httpConfig,
                    errorConverters
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
        WebDriverConfig {
            HttpClient {
                config(
                    jsonConfig,
                    httpConfig,
                    errorConverters
                )(this as HttpClientConfig<HttpClientEngineConfig>)
            }
        }
    }

private fun <T : HttpClientEngineConfig> config(
    jsonConfig: JsonBuilder.() -> Unit,
    httpConfig: HttpClientConfig<T>.() -> Unit,
    errorConverters: List<ErrorConverter> = listOf()
): HttpClientConfig<T>.() -> Unit = {
    val converters = errorConverters.reversed() + defaultConverter()

    install(ContentNegotiation) {
        json(kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            jsonConfig(this)
        })
    }
    expectSuccess = false

    HttpResponseValidator {
        this.validateResponse {
            if (!it.status.isSuccess()) {
                val body = it.body<JsonElement>()
                val status = it.status.value
                throw converters
                    .firstNotNullOf { it(status, body) }
            }
        }
    }
    httpConfig()
}
