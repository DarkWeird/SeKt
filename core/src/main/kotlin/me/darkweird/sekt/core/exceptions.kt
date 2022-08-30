package me.darkweird.sekt.core

import kotlinx.serialization.json.JsonElement


class WebDriverException(
    message: String,
    val kind: ErrorKind,
    val stacktrace: String? = null,
    val data: Any? = null
) : Exception("$message: $kind")

interface ErrorKind

typealias ErrorConverter = suspend (Int, JsonElement) -> WebDriverException?

data class UnknownErrorKind(val code: Int, val error: String) : ErrorKind

fun defaultConverter(): ErrorConverter = { code, body ->
    val value = body.asObj()?.get("value")?.asObj()
    if (value != null) {
        WebDriverException(
            value["message"].getString(),
            UnknownErrorKind(code, value["error"].getString()),
            value["stacktrace"].getString(),
            value["data"]
        )
    } else {
        WebDriverException(
            "This response - non-W3C response, cannot deserialize as Success nor Error",
            UnknownErrorKind(code, "unknown"),
            stacktrace = ""
        )
    }
}

