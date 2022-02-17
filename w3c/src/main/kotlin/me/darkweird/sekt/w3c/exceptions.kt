package me.darkweird.sekt.w3c

import me.darkweird.sekt.core.*

enum class W3CError(val httpCode: Int, val error: String) : ErrorKind {
    ELEMENT_CLICK_INTERCEPTED(400, "element click intercepted"),
    ELEMENT_NOT_INTERACTABLE(400, "element not interactable"),
    INSECURE_CERTIFICATE(400, "insecure certificate"),
    INVALID_ARGUMENT(400, "invalid argument"),
    INVALID_COOKIE_DOMAIN(400, "invalid cookie domain"),
    INVALID_ELEMENT_STATE(400, "invalid element state"),
    INVALID_SELECTOR(400, "invalid selector"),
    INVALID_SESSION_ID(404, "invalid session id"),
    JAVASCRIPT_ERROR(500, "javascript error"),
    MOVE_TARGET_OUT_OF_BOUNDS(500, "move target out of bounds"),
    NO_SUCH_ALERT(404, "no such alert"),
    NO_SUCH_COOKIE(404, "no such cookie"),
    NO_SUCH_ELEMENT(404, "no such element"),
    NO_SUCH_FRAME(404, "no such frame"),
    NO_SUCH_WINDOW(404, "no such window"),
    SCRIPT_TIMEOUT_ERROR(500, "script timeout"),
    SESSION_NOT_CREATED(500, "session not created"),
    STALE_ELEMENT_REFERENCE(404, "stale element reference"),
    TIMEOUT(500, "timeout"),
    UNABLE_TO_SET_COOKIE(500, "unable to set cookie"),
    UNABLE_TO_CAPTURE_SCREEN(500, "unable to capture screen"),
    UNEXPECTED_ALERT_OPEN(500, "unexpected alert open"),
    UNKNOWN_COMMAND(404, "unknown command"),
    UNKNOWN_ERROR(500, "unknown error"),
    UNKNOWN_METHOD(405, "unknown method"),
    UNSUPPORTED_OPERATION(500, "unsupported operation"),
}

fun w3cConverter(): ErrorConverter = { code, body ->
    body.asObj()
        ?.get("value")
        ?.asObj()?.let { value ->
            W3CError.values().firstOrNull {
                it.httpCode == code &&
                        it.error == value["error"]?.getString()
            }?.let {
                WebDriverException(
                    value["message"].getString(),
                    it,
                    value["stacktrace"].getString(),
                    value["data"]
                )
            }
        }

}
