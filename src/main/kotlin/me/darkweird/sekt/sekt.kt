package me.darkweird.sekt

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import kotlinx.serialization.json.JsonBuilder

public fun <T : HttpClientEngineConfig> webdriver(
    baseUrl: String,
    ktorEngine: HttpClientEngineFactory<T>,
    httpConfig: HttpClientConfig<T>.() -> Unit = {},
    jsonConfig: JsonFeature.Config.() -> Unit = {}
): WebDriver<HttpClient> =
    WebDriver(baseUrl) {
        WebDriverConfig {
            HttpClient(ktorEngine) {
                install(JsonFeature, jsonConfig)
                httpConfig()
            }
        }
    }


public fun webdriver(
    baseUrl: String,
    httpConfig: HttpClientConfig<*>.() -> Unit = {},
    jsonConfig: JsonBuilder.() -> Unit = {}
): WebDriver<HttpClient> =
    WebDriver(baseUrl) {
        WebDriverConfig {
            HttpClient {
                install(JsonFeature) {
                    serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                        jsonConfig()
                        encodeDefaults = false
                    })
                }
                httpConfig()
            }

        }
    }

