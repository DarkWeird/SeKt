package me.darkweird.sekt

import kotlinx.serialization.Serializable


//region new session

suspend fun <T, E> WebDriver<T, E>.create(parameters: LegacyNewSessionParameters): T
        where T : Session<E>,
              E : WebElement<E> =
    createSession(post("$baseUrl/session", parameters))

suspend fun <T, E> WebDriver<T, E>.create(parameters: WebDriverNewSessionParameters): T
        where T : Session<E>,
              E : WebElement<E> =
    createSession(post("$baseUrl/session", parameters))

//endregion

//region status
@Serializable
class Status(val ready: Boolean, val message: String)

suspend fun WebDriver<*, *>.status(): WebDriverResult<Status> = get("$baseUrl/status")
//endregion

//region delete session

suspend fun WebDriver<*, *>.deleteSession(sessionId: String): WebDriverResult<Empty?> =
    delete("$baseUrl/session/${sessionId}")
//endregion

