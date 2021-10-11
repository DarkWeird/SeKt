package me.darkweird.sekt.w3c

import kotlinx.coroutines.delay
import java.util.concurrent.TimeoutException
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@ExperimentalTime
public suspend inline fun wait(
    period: Duration = Duration.seconds(1),
    timeout: Duration = Duration.minutes(1),
    condition: () -> Boolean
) {
    val times = (timeout / period).toInt()
    (0..times).forEach {
        if (condition()) {
            return
        }
        delay(period)
    }
    throw TimeoutException()
}


@ExperimentalTime
public suspend inline fun <T> T.waitUntil(
    period: Duration = Duration.seconds(1),
    timeout: Duration = Duration.minutes(1),
    condition: T.() -> Boolean
): T {
    wait(period, timeout) {

        condition(this)
    }
    return this
}
