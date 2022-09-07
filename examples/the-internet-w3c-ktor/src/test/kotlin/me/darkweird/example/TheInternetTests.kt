package me.darkweird.example

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.ktor.client.engine.cio.*
import me.darkweird.sekt.core.Session
import me.darkweird.sekt.core.WebDriver
import me.darkweird.sekt.core.capabilities
import me.darkweird.sekt.w3c.W3CCapabilities.browserName
import me.darkweird.sekt.w3c.W3CCapabilities.platformName
import me.darkweird.sekt.w3c.session
import me.darkweird.sekt.w3c.w3cConverter
import kotlin.time.ExperimentalTime


const val wdUrl: String = "http://localhost:4444/wd/hub"

private suspend fun withTestSession(block: suspend Session.() -> Unit) {
    WebDriver(wdUrl) {
        webdriver {
            addErrorConverter(w3cConverter())
        }
    }
        .session(
            capabilities
            {
                browserName = "firefox"
                platformName = "linux"
            },
            block
        )
}

@OptIn(ExperimentalKotest::class)
@ExperimentalTime
class TheInternetTests : FunSpec() {
    init {
        concurrency = 5
        theInternetTests { _, s ->
            withTestSession(s)
        }
    }
}
