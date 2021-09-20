package me.darkweird.sekt.w3c

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.darkweird.sekt.Caps
import me.darkweird.sekt.capability

const val FRAME_KEY = "frame-075b-4da1-b6ba-e579c2d3230a"
const val WINDOW_KEY = "window-fcc6-11e5-b4f8-330a88ab9d7f"
const val ELEMENT_KEY = "element-6066-11e4-a52e-4f735466cecf"


object W3CCapabilities {
    var Caps.browserName: String by capability()
    var Caps.browserVersion: String by capability()
    var Caps.platformName: String by capability()
    var Caps.acceptInsecureCerts: Boolean by capability()
    var Caps.pageLoadStrategy: String by capability()
    var Caps.proxy: Proxy by capability()
    var Caps.setWindowRect: Boolean by capability()
    var Caps.timeouts: Timeouts by capability()
    var Caps.unhandledPromptBehavior: String by capability()
}


@Serializable
sealed class Proxy(val proxyType: String) {
    @Serializable
    data class PAC(val proxyAutoconfigUrl: String) : Proxy("pac")

    @Serializable
    object Direct : Proxy("direct")

    @Serializable
    object Autodetect : Proxy("autodetect")

    @Serializable
    object System : Proxy("system")

    @Serializable
    data class Manual(
        val ftpProxy: String,
        val httpProxy: String,
        val noProxy: List<String>,
        val sslProxy: String,
        val socksProxy: String,
        val socksVersion: Int
    ) : Proxy("manual")
}


@Serializable
data class Status(
    val ready: Boolean,
    val message: String
)

@Serializable
data class WebElementResponse(
    @SerialName(ELEMENT_KEY)
    val elementId: String
)

@Serializable
data class Cookie(
    val name: String,
    val value: String,
    val path: String = "/",
    val domain: String? = null,
    val secure: Boolean? = null,
    val httpOnly: Boolean? = null,
    val expiry: Long? = null,
)

@Serializable
data class CookieData(val cookie: Cookie)

@Serializable
sealed class Action {
    @Serializable
    @SerialName("key")
    data class KeyAction(val id: String, val actions: List<KeyActionItem>) : Action()

    @Serializable
    @SerialName("pointer")
    data class PointAction(val id: String) : Action()


    @Serializable
    @SerialName("none")
    data class NoneAction(val id: String, val actions: List<GeneralAction>) : Action()
}

@Serializable
sealed class KeyActionItem {

    @Serializable
    @SerialName("keyUp")
    data class KeyUpAction(val value: String) : KeyActionItem()

    @Serializable
    @SerialName("keyDown")
    data class KeyDownAction(val value: String) : KeyActionItem()

}

@Serializable
sealed class GeneralAction {
    @Serializable
    @SerialName("pause")
    data class PauseAction(val duration: Long) : GeneralAction()
}

@Serializable
data class Actions(val actions: List<Action>)

@Serializable
data class Timeouts(
    val script: Int? = null,
    val pageLoad: Int? = null,
    val implicit: Int? = null
)

@Serializable
class PageUrl(val url: String)

@Serializable
class WindowHandle(val handle: String) // TODO seems there can be another variants

@Serializable
sealed class FrameLocator {
    @Serializable
    class IdLocator(val id: Int)

    @Serializable
    class WebElementLocator(val id: String)
}

fun css(value: String) = Locator("css selector", value)
fun linkText(value: String) = Locator("link text", value)
fun partialLinkText(value: String) = Locator("partial link text", value)
fun tagName(value: String) = Locator("tag name", value)
fun xpath(value: String) = Locator("xpath", value)


@Serializable
data class Rect<T : Number>(
    val x: T,
    val y: T,
    val width: T,
    val height: T
)

@Serializable
data class Locator(
    val using: String,
    val value: String
)

@Serializable
data class Text(val text: String)

@Serializable
data class ScriptData(
    val script: String,
    val args: List<JsonElement>
)