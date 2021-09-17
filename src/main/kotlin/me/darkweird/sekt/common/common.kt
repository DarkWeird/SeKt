package me.darkweird.sekt

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject


typealias Capabilities = JsonObject // TODO make generic serialization

@Serializable
class WebDriverNewSessionParameters(
    val capabilities: WebDriverCapabilities,
)

@Serializable
class WebDriverCapabilities(
    val alwaysMatch: Capabilities? = null,
    val firstMatch: List<Capabilities>? = null
)

@Serializable
class CreateSessionResponse(
    val sessionId: String,
    val capabilities: Capabilities
)

interface SessionFactory<T, R> {
    suspend fun create(driver: T, capabilities: WebDriverNewSessionParameters): R
}

class WebDriverConfig<T>(
    val executorFactory: () -> T
)

class WebDriver<T>(
    val baseUrl: String,
    configFactory: () -> WebDriverConfig<T>
) {
    private val config: WebDriverConfig<T> = configFactory()
    val executor: T = this.config.executorFactory()
}

class Session<T>(
    val sessionId: String,
    val webDriver: WebDriver<T>
)

class WebElement<T>(
    val elementId: String,
    val session: Session<T>
)

interface SuspendableClosable {
    suspend fun close()
}

suspend fun <T, R> WebDriver<T>.session(
    factory: SessionFactory<WebDriver<T>, R>,
    caps: WebDriverNewSessionParameters
): R =
    factory.create(this, caps)


suspend fun <T, R : SuspendableClosable> WebDriver<T>.session(
    factory: SessionFactory<WebDriver<T>, R>,
    caps: WebDriverNewSessionParameters,
    block: suspend R.() -> Unit
) {
    val session = session(factory, caps)
    try {
        block(session)
    } finally {
        session.close()
    }
}


