package me.darkweird.sekt

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.mapSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.reflect.KProperty


typealias Capabilities = JsonObject // TODO make generic serialization

@Serializable
data class WebDriverNewSessionParameters(
    val capabilities: WebDriverCapabilities,
)

@Serializable
data class WebDriverCapabilities(
    val alwaysMatch: Capabilities? = null,
    val firstMatch: List<Capabilities> = listOf()
)

@Serializable
data class CreateSessionResponse(
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


@OptIn(InternalSerializationApi::class)
public inline fun <reified T : Any> capability(): Caps.Capability<T> {
    return Caps.Capability(T::class.serializer())
}

@Serializable(Ser::class)
class Caps {
    internal val mutableMap: MutableMap<String, Pair<KSerializer<*>, *>> = mutableMapOf()

    class Capability<T : Any>(private val serializer: KSerializer<T>) {

        operator fun getValue(thisRef: Caps, property: KProperty<*>): T {
            return thisRef.mutableMap[property.name] as T
        }

        operator fun setValue(thisRef: Caps, property: KProperty<*>, value: T) {
            thisRef.mutableMap[property.name] = Pair(serializer, value)
        }
    }


}

object Ser : KSerializer<Caps> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = mapSerialDescriptor<String, JsonElement>()

    override fun deserialize(decoder: Decoder): Caps {
        throw UnsupportedOperationException("Capabilities deserialization don't supported")
    }

    override fun serialize(encoder: Encoder, value: Caps) {
        encoder.encodeStructure(descriptor) {
            var index = 0
            value.mutableMap.forEach { (k, v) ->
                encodeStringElement(descriptor, index++, k)
                encodeSerializableElement(descriptor, index++, v.first as KSerializer<Any>, v.second as Any)
            }
        }
    }
}

fun capabilities(
    firstMatch: List<Caps.() -> Unit> = listOf(),
    alwaysMatch: Caps.() -> Unit
): WebDriverNewSessionParameters =
    WebDriverNewSessionParameters(
        WebDriverCapabilities(
            Json.encodeToJsonElement(Caps().also(alwaysMatch)) as Capabilities,
            firstMatch.map { Json.encodeToJsonElement(Caps().also(it)) as Capabilities }
        )
    )



