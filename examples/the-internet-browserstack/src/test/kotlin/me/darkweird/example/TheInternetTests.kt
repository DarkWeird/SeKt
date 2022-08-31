package me.darkweird.example

import io.kotest.common.ExperimentalKotest
import io.kotest.common.runBlocking
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.string.shouldContain
import io.kotest.mpp.env
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import me.darkweird.browserstack.Browser
import me.darkweird.browserstack.BrowserStackApi
import me.darkweird.browserstack.Status
import me.darkweird.browserstack.StatusRequest
import me.darkweird.sekt.browserstack.BSSession
import me.darkweird.sekt.browserstack.BrowserStackCapabilities.browserStack
import me.darkweird.sekt.browserstack.BrowserstackSessionCreator
import me.darkweird.sekt.browserstack.browserStack
import me.darkweird.sekt.browserstack.browserstack
import me.darkweird.sekt.core.capabilities
import me.darkweird.sekt.core.session
import me.darkweird.sekt.w3c.*
import me.darkweird.sekt.w3c.W3CCapabilities.browserName
import me.darkweird.sekt.w3c.W3CCapabilities.browserVersion
import me.darkweird.sekt.w3c.W3CCapabilities.platformName

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
class TheInternetTests : FunSpec() {
    init {
        concurrency = 4
        theInternetTests(::withTestSession)
    }
}


@OptIn(ExperimentalKotest::class)
class RunAtAllBrowserTests : FunSpec() {
    init {

        val browserstack = browserstack(
            "https://hub-cloud.browserstack.com/wd/hub",
            errorConverters = listOf(w3cConverter()),
            httpConfig = {
                install(Logging) {
                    level = LogLevel.ALL
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 60_000
                }
            },
            username = env("BS_USERNAME")
                ?: throw IllegalArgumentException("env variable BS_USERNAME should be provided"),
            password = env("BS_PASSWORD")
                ?: throw IllegalArgumentException("env variable BS_PASSWORD should be provided")
        )

        val api = BrowserStackApi.create(browserstack.executor)

        //Setup concurrency as your plan can
        dispatcherAffinity = true
        concurrency = runBlocking { api.getPlanDetails() }.parallelSessionsMaxAllowed

        val browsers = runBlocking { api.getBrowsers() }
            .filter {
                if (it.realMobile != null) { // Filter out mobiles, we haven't appium's capabilities (device name)
                    false
                } else if (it.os == "OS X") { // Filter out Mac - browserstack provides non-selenium parameters
                    false
                } else {
                    when (it.browser) { // Filter out outdated webdrivers
                        "firefox" -> (it.browserVersion?.substringBefore(".")?.toInt() ?: 0) > 58
                        "chrome" -> (it.browserVersion?.substringBefore(".")?.toInt() ?: 0) > 90
                        "edge" -> (it.browserVersion?.substringBefore(".")?.toInt() ?: 0) > 17
                        "opera" -> false
                        "ie" -> false
                        else -> true
                    }
                }
            }

        withData(
            { "${it.os} ${it.osVersion} - ${it.browser} - ${it.browserVersion}" },
            browsers
        ) { browser: Browser ->
            browserstack.session(
                BrowserstackSessionCreator,
                capabilities {
                    platformName = browser.os
                    browserName = browser.browser
                    browser.browserVersion?.let {
                        browserVersion = it
                    }
                    browserStack = browserStack {
                        projectName = "Sekt"
                        buildName = "All Browsers test - $time"
                    }
                }
            ) {
                runCatching {
                    setUrl(PageUrl("http://the-internet.herokuapp.com/login"))

                    findElement(css("#username")).sendKeys(Text("tomsmith"))
                    findElement(css("#password")).sendKeys(Text("SuperSecretPassword!"))
                    findElement(css("#login button")).click()

                    findElement(css("#flash")).getText() shouldContain "You logged into a secure area!"
                }.onSuccess {
                    api.session.setTestStatus(sessionId, StatusRequest(Status.PASSED, "Test passed!"))
                }.onFailure {
                    api.session.setTestStatus(
                        sessionId,
                        StatusRequest(Status.FAILED, it.message ?: "Unknown error")
                    )
                }.getOrThrow()
            }
        }
    }
}