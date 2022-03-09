package me.darkweird.sekt.core

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable


@Serializable
data class WebDriverResponse<T>(
    val value: T
)

@Serializable
object Empty

suspend inline fun <reified R> WebDriver.get(path: String): R {
    return executor.get(baseUrl + path).body<WebDriverResponse<R>>().value
}

suspend inline fun <reified T : Any, reified R> WebDriver.post(path: String, body: T): R =
    if (R::class == Unit::class) {
        executor.post(baseUrl + path) {
            setBody(body)
            contentType(ContentType.Application.Json)
        }.body<WebDriverResponse<R>>().value
        Unit as R
    } else {
        executor.post(baseUrl + path) {
            setBody(body)
            contentType(ContentType.Application.Json)
        }.body<WebDriverResponse<R>>().value
    }


suspend inline fun <reified R> WebDriver.delete(path: String): R =
    if (R::class == Unit::class) {
        executor.delete(baseUrl + path).body<WebDriverResponse<Empty?>>().value
        Unit as R
    } else {
        executor.delete(baseUrl + path).body<WebDriverResponse<R>>().value
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





