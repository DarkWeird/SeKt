package me.darkweird.sekt.core

import io.ktor.client.*
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
import kotlin.collections.set
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

interface SessionFactory<R> {
    suspend fun create(driver: WebDriver, capabilities: WebDriverNewSessionParameters): R
}

open class WebDriver(
    val baseUrl: String,
    configFactory: WebDriverBuilder.() -> Unit
) {
    val executor: HttpClient
    val errorConverters: List<ErrorConverter>
    val json: Json

    init {
        val builder = WebDriverBuilder()
        configFactory(builder)
        executor = builder.httpClientProvider()
        errorConverters = builder.webDriverConfig.errorConverters.reversed()
        json = builder.jsonProvider()
    }

}

open class Session(
    val sessionId: String,
    val webDriver: WebDriver
) : SuspendableClosable {
    override suspend fun close() {
        webDriver.delete<String?>("/session/$sessionId") // TODO
    }
}

class WebElement(
    var elementId: String,
    val session: Session,
    val refreshFn: suspend WebElement.() -> Unit
)

interface SuspendableClosable {
    suspend fun close()
}

suspend fun <R> WebDriver.session(
    factory: SessionFactory<R>,
    caps: WebDriverNewSessionParameters
): R =
    factory.create(this, caps)

suspend fun <R : SuspendableClosable> WebDriver.session(
    factory: SessionFactory<R>,
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
inline fun <reified T : Any> capability(name: String? = null): Caps.Capability<T> {
    return Caps.Capability(T::class.serializer(), name)
}

@Serializable(Ser::class)
class Caps {
    internal val mutableMap: MutableMap<String, Pair<KSerializer<*>, *>> = mutableMapOf()

    class Capability<T : Any>(private val serializer: KSerializer<T>, private val name: String?) {

        operator fun getValue(thisRef: Caps, property: KProperty<*>): T {
            return thisRef.mutableMap[name ?: property.name] as T
        }

        operator fun setValue(thisRef: Caps, property: KProperty<*>, value: T) {
            thisRef.mutableMap[name ?: property.name] = Pair(serializer, value)
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
            Json.encodeToJsonElement(
                Caps().also(alwaysMatch)
            ) as Capabilities,
            firstMatch.map {
                Json.encodeToJsonElement(
                    Caps().also(it)
                ) as Capabilities
            }
        )
    )



