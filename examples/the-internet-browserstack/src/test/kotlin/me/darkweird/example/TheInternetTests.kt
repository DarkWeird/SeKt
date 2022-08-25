package me.darkweird.example

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.mpp.env
import io.ktor.client.plugins.logging.*
import me.darkweird.browserstack.Status
import me.darkweird.browserstack.StatusRequest
import me.darkweird.sekt.browserstack.BSSession
import me.darkweird.sekt.browserstack.BrowserStackCapabilities.browserStack
import me.darkweird.sekt.browserstack.BrowserstackSessionCreator
import me.darkweird.sekt.browserstack.browserStack
import me.darkweird.sekt.browserstack.browserstack
import me.darkweird.sekt.core.*
import me.darkweird.sekt.w3c.*
import me.darkweird.sekt.w3c.W3CCapabilities.browserName
import me.darkweird.sekt.w3c.W3CCapabilities.browserVersion
import me.darkweird.sekt.w3c.W3CCapabilities.platformName
import kotlin.time.ExperimentalTime

private val time = java.time.LocalDateTime.now()

private suspend fun withTestSession(
    displayName: String? = null,
    block: suspend BSSession.() -> Unit
) {
    val browserstack = browserstack(
        "https://hub-cloud.browserstack.com/wd/hub",
        errorConverters = listOf(w3cConverter()),
        httpConfig = {
            install(Logging) {
                level = LogLevel.ALL
            }
        },
        username = env("BS_USERNAME") ?: throw IllegalArgumentException("env variable BS_USERNAME should be provided"),
        password = env("BS_PASSWORD") ?: throw IllegalArgumentException("env variable BS_PASSWORD should be provided")
    )
    val choosedBrowser =
        browserstack

    browserstack.session(
        BrowserstackSessionCreator,
        capabilities
        {
            browserName = "firefox"
            platformName = "MAC"
            browserVersion = "97.0"
            browserStack = browserStack {
                projectName = "Sekt"
                buildName = "The Internet tests - $time"
                sessionName = displayName
            }
        }
    ) {
        runCatching {
            block.invoke(this)
        }.onSuccess {
            api.session.setTestStatus(sessionId, StatusRequest(Status.PASSED, "Test passed!"))
        }.onFailure {
            api.session.setTestStatus(sessionId, StatusRequest(Status.FAILED, it.message ?: "Unknown error"))
        }.getOrThrow()
    }
}


@OptIn(ExperimentalKotest::class)
@ExperimentalTime
class TheInternetTests : FunSpec() {
    init {
        concurrency = 4
        theInternetTests(::withTestSession)
    }
}
