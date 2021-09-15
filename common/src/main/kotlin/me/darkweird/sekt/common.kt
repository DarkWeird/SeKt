package me.darkweird.sekt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject


typealias Capabilities = JsonObject // TODO make generic serialization

@Serializable
class LegacyNewSessionParameters(
    val desiredCapabilities: Capabilities,
    val requiredCapabilities: Capabilities? = null
)

@Serializable
class WebDriverNewSessionParameters(
    val capabilities: WebDriverCapabilities,
)

@Serializable
class WebDriverCapabilities(
    val allMatch: Capabilities? = null,
    val firstMatch: List<Capabilities>? = null
)

@Serializable
class CreateSessionResponse(
    val sessionId: String,
    val capabilities: Capabilities
)

const val ELEMENT_KEY = "element-6066-11e4-a52e-4f735466cecf";

@Serializable
class WebElementResponse(
    @SerialName(ELEMENT_KEY)
    val elementId: String
)


interface Session<E>
        where E : WebElement<E> {
    val baseUrl: String
    val uuid: String
    val createWebElement: (WebDriverResult<WebElementResponse>) -> E
}

interface WebDriver<S, E>
        where S : Session<E>,
              E : WebElement<E> {
    val baseUrl: String
    val createSession: (WebDriverResult<CreateSessionResponse>) -> S
}

interface WebElement<E>
        where E : WebElement<E> {
    val baseUrl: String
    val uuid: String
    val elementId: String
}

