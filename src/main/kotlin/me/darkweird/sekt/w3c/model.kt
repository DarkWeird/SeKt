package me.darkweird.sekt.w3c

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.mapSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonElement
import me.darkweird.sekt.Caps
import me.darkweird.sekt.Empty
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
sealed class SwitchToFrame {

    @Serializable(NullIdSerializer::class)
    class Null : SwitchToFrame() // should be class, not object, Serializable don't works otherwise


    @Serializable
    data class Number(val id: Int) : SwitchToFrame()

    @Serializable
    class WebElement : SwitchToFrame {
        private val id: WebElementObject

        private constructor(id: WebElementObject) {
            this.id = id
        }

        constructor(element: me.darkweird.sekt.WebElement) {
            this.id = WebElementObject(element.elementId)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
object NullIdSerializer : KSerializer<SwitchToFrame.Null> {
    override val descriptor: SerialDescriptor = mapSerialDescriptor<String, Empty?>()
    override fun deserialize(decoder: Decoder): SwitchToFrame.Null {
        throw UnsupportedOperationException("SwitchToFrame.Null not deserializable")
    }

    override fun serialize(encoder: Encoder, value: SwitchToFrame.Null) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, "id")
            encodeNullableSerializableElement(descriptor, 1, Empty.serializer(), null)
        }
    }
}

@Serializable
data class Status(
    val ready: Boolean,
    val message: String
)

@Serializable
data class WebElementObject(
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
    data class PointerAction(val id: String, val actions: List<PointerActionItem>) : Action()


    @Serializable
    @SerialName("none")
    data class NoneAction(val id: String, val actions: List<GeneralAction>) : Action()
}

@Serializable
sealed class PointerActionItem {
    @Serializable
    @SerialName("pointerUp")
    data class Up(val button: Int) : PointerActionItem()

    @Serializable
    @SerialName("pointerDown")
    data class Down(val button: Int) : PointerActionItem()

    @Serializable
    @SerialName("pointerMove")
    data class Move(
        val duration: Int,
        val origin: Origin,
        val x: Int,
        val y: Int
    ) : PointerActionItem()

    @Serializable
    @SerialName("pointerCancel")
    class Cancel : PointerActionItem()
}

@Serializable(OriginSerializer::class)
sealed class Origin {
    object ViewPort : Origin()
    object Pointer : Origin()
    class WebElement(val element: WebElementObject) : Origin()
}

object OriginSerializer : KSerializer<Origin> {
    override fun deserialize(decoder: Decoder): Origin {
        throw UnsupportedOperationException("Origin cannot be deserialized")
    }

    override val descriptor: SerialDescriptor = serialDescriptor<Origin>()

    override fun serialize(encoder: Encoder, value: Origin) {
        when (value) {
            Origin.Pointer -> encoder.encodeString("pointer")
            Origin.ViewPort -> encoder.encodeString("viewport")
            is Origin.WebElement -> encoder.encodeSerializableValue(WebElementObject.serializer(), value.element)
        }
    }
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