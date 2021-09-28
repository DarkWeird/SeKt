package me.darkweird.sekt

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonElement
import me.darkweird.sekt.common.ErrorConverter
import me.darkweird.sekt.common.defaultConverter


public fun <T : HttpClientEngineConfig> webdriver(
    baseUrl: String,
    ktorEngine: HttpClientEngineFactory<T>,
    httpConfig: HttpClientConfig<T>.() -> Unit = {},
    jsonConfig: JsonFeature.Config.() -> Unit = {}
): WebDriver<HttpClient> = webdriver(baseUrl, listOf(), ktorEngine, httpConfig, jsonConfig)

public fun <T : HttpClientEngineConfig> webdriver(
    baseUrl: String,
    errorConverters: List<ErrorConverter>,
    ktorEngine: HttpClientEngineFactory<T>,
    httpConfig: HttpClientConfig<T>.() -> Unit = {},
    jsonConfig: JsonFeature.Config.() -> Unit = {}
): WebDriver<HttpClient> =
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

public fun webdriver(
    baseUrl: String,
    errorConverters: List<ErrorConverter> = listOf(),
    httpConfig: HttpClientConfig<HttpClientEngineConfig>.() -> Unit = {},
    jsonConfig: JsonFeature.Config.() -> Unit = {},
): WebDriver<HttpClient> =
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