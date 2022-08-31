package me.darkweird.sekt.core

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement


@Serializable
data class WebDriverResponse<T>(
    val value: T
)

@Serializable
object Empty


suspend inline fun <reified R> WebDriver.get(path: String): R {
    return executor.get(baseUrl + path).decodeWebDriver(json, errorConverters)
}

suspend inline fun <reified T : Any, reified R> WebDriver.post(path: String, body: T): R {
    val resp = executor.post(baseUrl + path) {
        setBody(body)
        contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
    }
    return decodeNullable(resp, json, errorConverters)
}


suspend inline fun <reified R> WebDriver.delete(path: String): R {
    val resp = executor.delete(baseUrl + path)
    return decodeNullable(resp, json, errorConverters)
}

suspend inline fun <reified R> Session.get(path: String): R =
    webDriver.get("/session/$sessionId$path")

suspend inline fun <reified R> Session.delete(path: String): R =
    webDriver.delete("/session/$sessionId$path")

suspend inline fun <reified T : Any, reified R> Session.post(path: String, body: T): R =
    webDriver.post("/session/$sessionId$path", body)

suspend inline fun <reified R> WebElement.get(path: String): R =
    session.get("/element/$elementId$path")

suspend inline fun <reified R> WebElement.delete(path: String): R =
    session.delete("/element/$elementId$path")


suspend inline fun <reified T : Any, reified R> WebElement.post(path: String, body: T): R =
    session.post("/element/$elementId$path", body)

suspend inline fun <reified R> HttpResponse.decodeWebDriver(
    deserializer: Json,
    errorConverters: List<ErrorConverter>
): R {
    val json = body<JsonElement>()
    try {
        return deserializer.decodeFromJsonElement<WebDriverResponse<R>>(json).value
    } catch (e: SerializationException) {
        throw errorConverters
            .firstNotNullOf { it(this.status.value, json) }
    }
}

suspend inline fun <reified R> decodeNullable(
    resp: HttpResponse,
    deserializer: Json,
    converters: List<ErrorConverter>
): R {
    return if (R::class == Unit::class) {
        resp.decodeWebDriver<Empty?>(deserializer, converters)
        Unit as R
    } else {
        resp.decodeWebDriver(deserializer, converters)
    }
}
