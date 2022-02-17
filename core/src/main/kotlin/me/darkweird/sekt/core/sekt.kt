package me.darkweird.sekt.core

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonElement


fun <T : HttpClientEngineConfig> webdriver(
    baseUrl: String,
    ktorEngine: HttpClientEngineFactory<T>,
    httpConfig: HttpClientConfig<T>.() -> Unit = {},
    jsonConfig: JsonFeature.Config.() -> Unit = {}
): WebDriver = webdriver(baseUrl, listOf(), ktorEngine, httpConfig, jsonConfig)

fun <T : HttpClientEngineConfig> webdriver(
    baseUrl: String,
    errorConverters: List<ErrorConverter>,
    ktorEngine: HttpClientEngineFactory<T>,
    httpConfig: HttpClientConfig<T>.() -> Unit = {},
    jsonConfig: JsonFeature.Config.() -> Unit = {}
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
    jsonConfig: JsonFeature.Config.() -> Unit = {},
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
    jsonConfig: JsonFeature.Config.() -> Unit,
    httpConfig: HttpClientConfig<T>.() -> Unit,
    errorConverters: List<ErrorConverter> = listOf()
): HttpClientConfig<T>.() -> Unit = {
    val converters = errorConverters.reversed() + defaultConverter()

    install(JsonFeature, jsonConfig)
    expectSuccess = false
    HttpResponseValidator {
        this.validateResponse {
            if (!it.status.isSuccess()) {
                val body = it.receive<JsonElement>()
                val status = it.status.value
                throw converters
                    .firstNotNullOf { it(status, body) }
            }
        }
    }
    httpConfig()
}
