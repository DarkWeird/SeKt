package me.darkweird.sekt

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable


@Serializable
data class WebDriverResponse<T>(val value: T)

@Serializable
object Empty

suspend inline fun <reified R> WebDriver<HttpClient>.get(path: String): R {
    return executor.get<WebDriverResponse<R>>(baseUrl + path).value
}

suspend inline fun <T : Any, reified R> WebDriver<HttpClient>.post(path: String, body: T): R =
    if (R::class == Unit::class) {
        executor.post<WebDriverResponse<Empty?>>(baseUrl + path) {
            this.body = body
            contentType(ContentType.parse("application/json"))
        }.value
        Unit as R
    } else {
        executor.post<WebDriverResponse<R>>(baseUrl + path) {
            this.body = body
            contentType(ContentType.parse("application/json"))
        }.value
    }


suspend inline fun <reified R> WebDriver<HttpClient>.delete(path: String): R =
    if (R::class == Unit::class) {
        executor.delete<WebDriverResponse<Empty?>>(baseUrl + path).value
        Unit as R
    } else {
        executor.delete<WebDriverResponse<R>>(baseUrl + path).value
    }

suspend inline fun <reified R> Session<HttpClient>.get(path: String): R =
    webDriver.get("/session/$sessionId$path")

suspend inline fun <reified R> WebElement<HttpClient>.get(path: String): R =
    session.get("/element/$elementId$path")


suspend inline fun <reified R> Session<HttpClient>.delete(path: String): R =
    webDriver.delete("/session/$sessionId$path")

suspend inline fun <reified R> WebElement<HttpClient>.delete(path: String): R =
    session.delete("/element/$elementId$path")

suspend inline fun <T : Any, reified R> Session<HttpClient>.post(path: String, body: T): R =
    webDriver.post("/session/$sessionId$path", body)

suspend inline fun <T : Any, reified R> WebElement<HttpClient>.post(path: String, body: T): R =
    session.post("/element/$elementId$path", body)



