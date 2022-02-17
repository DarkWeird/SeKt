package me.darkweird.sekt.w3c

import kotlinx.coroutines.delay
import java.util.concurrent.TimeoutException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime


@ExperimentalTime
suspend inline fun wait(
    period: Duration = 1.seconds,
    timeout: Duration = 1.minutes,
    condition: () -> Boolean
) {
    val times = (timeout / period).toInt()
    repeat((0..times).count()) {
        if (condition()) {
            return
        }
        delay(period)
    }
    throw TimeoutException()
}


@ExperimentalTime
suspend inline fun <T> T.waitUntil(
    period: Duration = 1.seconds,
    timeout: Duration = 1.minutes,
    condition: T.() -> Boolean
): T {
    wait(period, timeout) {

        condition(this)
    }
    return this
}
