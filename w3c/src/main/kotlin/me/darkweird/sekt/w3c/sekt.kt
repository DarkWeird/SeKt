package me.darkweird.sekt.w3c

import me.darkweird.sekt.core.Session
import me.darkweird.sekt.core.WebDriver
import me.darkweird.sekt.core.WebDriverNewSessionParameters
import me.darkweird.sekt.core.session


suspend fun WebDriver.session(
    caps: WebDriverNewSessionParameters
): Session =
    DefaultSessionCreator.create(this, caps)


suspend fun WebDriver.session(
    caps: WebDriverNewSessionParameters,
    block: suspend Session.() -> Unit
) {
    val session = session(DefaultSessionCreator, caps)
    try {
        block(session)
    } finally {
        session.close()
    }
}
