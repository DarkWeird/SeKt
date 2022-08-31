package me.darkweird.sekt.browserstack

import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import me.darkweird.browserstack.BrowserStackApi
import me.darkweird.sekt.core.*

const val browserstackUrl = "https://hub-cloud.browserstack.com/wd/hub"

object BrowserstackSessionCreator : SessionFactory<BSSession> {
    override suspend fun create(driver: WebDriver, capabilities: WebDriverNewSessionParameters): BSSession {
        val params = driver.post<WebDriverNewSessionParameters, CreateSessionResponse>("/session", capabilities)
        return BSSession(params.sessionId, driver)
    }
}

class BSSession(sessionId: String, webDriver: WebDriver) : Session(sessionId, webDriver) {
    val api: BrowserStackApi by lazy { BrowserStackApi.create(webDriver.executor) }
    override suspend fun close() {
        kotlin.runCatching { super.close() } // simple ignore this!
    }
}

fun browserstack(
    username: String, password: String,
    webDriverBuilder: WebDriverBuilder.() -> Unit
): WebDriver =
    WebDriver(browserstackUrl) {
        json {
            ignoreUnknownKeys = true
        }
        ktor {
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
                    realm = "/"
                }
            }
        }
        webDriverBuilder()
    }