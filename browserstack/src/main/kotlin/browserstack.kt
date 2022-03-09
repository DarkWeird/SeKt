package me.darkweird.sekt.browserstack

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import me.darkweird.browserstack.BrowserStackApi
import me.darkweird.sekt.core.*


object BrowserstackSessionCreator : SessionFactory<BSSession> {
    override suspend fun create(driver: WebDriver, capabilities: WebDriverNewSessionParameters): BSSession {
        val params = driver.post<WebDriverNewSessionParameters, CreateSessionResponse>("/session", capabilities)
        return BSSession(params.sessionId, driver)
    }
}

class BSSession(sessionId: String, webDriver: WebDriver) : Session(sessionId, webDriver) {
    val api : BrowserStackApi = BrowserStackApi.create(webDriver.executor)
}

fun browserstack(
    baseUrl: String,
    errorConverters: List<ErrorConverter> = listOf(),
    httpConfig: HttpClientConfig<HttpClientEngineConfig>.() -> Unit = {},
    username: String, password: String
): WebDriver =
    webdriver(baseUrl,
        errorConverters = errorConverters,
        httpConfig = {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(
                            username,
                            password
                        )
                    }
                    sendWithoutRequest {
                        it.url.host.endsWith("browserstack.com")
                    }
                    realm="/"
                }
            }
            httpConfig()
        })