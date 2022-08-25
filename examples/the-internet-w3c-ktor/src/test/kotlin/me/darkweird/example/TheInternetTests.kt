package me.darkweird.example

import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.ktor.client.engine.cio.*
import me.darkweird.sekt.core.*
import me.darkweird.sekt.w3c.*
import me.darkweird.sekt.w3c.W3CCapabilities.browserName
import me.darkweird.sekt.w3c.W3CCapabilities.platformName
import kotlin.time.ExperimentalTime


const val wdUrl: String = "http://localhost:4444"

private suspend fun withTestSession(block: suspend Session.() -> Unit) {
    webdriver(wdUrl, errorConverters = listOf(w3cConverter()), CIO, httpConfig = {
        engine {
            requestTimeout = 60_000
        }
    })
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
        concurrency = 1
        theInternetTests { _, s ->
            withTestSession(s)
        }
    }
}
