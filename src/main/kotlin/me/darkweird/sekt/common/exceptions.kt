package me.darkweird.sekt.common

import kotlinx.serialization.json.JsonElement


public class WebDriverException(
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
            "received json error without 'value' property",
            UnknownErrorKind(code, "unknown"),
            stacktrace = ""
        )
    }
}

