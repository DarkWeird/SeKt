package me.darkweird.sekt

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.reflect.*
import kotlinx.serialization.Serializable

@Serializable
object Empty

@Serializable
class WebserverResponse<T>(val value: T)

@Serializable
class WebDriverError(val error: String, val message: String, val stacktrace: String)

val client = HttpClient(CIO) {
    install(JsonFeature)
    expectSuccess = false
}

suspend inline fun <reified T> get(url: String): WebDriverResult<T> =
    handleResponse(HttpRequestBuilder().apply {
        method = HttpMethod.Get
        url {
            url(url)
        }
    })


suspend inline fun <reified R> post(url: String, body: Any): WebDriverResult<R> =
    handleResponse(HttpRequestBuilder().apply {
        method = HttpMethod.Post
        url {
            url(url)
        }
        this.body = body
        this.contentType(ContentType.parse("application/json"))
    })

suspend inline fun <reified T> delete(url: String): WebDriverResult<T> =
    handleResponse(HttpRequestBuilder().apply {
        method = HttpMethod.Delete
        url {
            url(url)
        }
    })

suspend inline fun <reified T> handleResponse(req: HttpRequestBuilder): WebDriverResult<T> =
    HttpStatement(req, client)
        .execute {
            if (it.status.isSuccess()) {
                val wdResponse = it.call.receive(typeInfo<WebserverResponse<T>>()) as WebserverResponse<T>
                WebDriverResult.Success(wdResponse.value)
            } else {
                val wdResponse =
                    it.call.receive(typeInfo<WebserverResponse<WebDriverError>>()) as WebserverResponse<WebDriverError>
                val error = wdResponse.value;
                WebDriverResult.Error(error.error, error.message, error.stacktrace, Empty)
            }
        }
