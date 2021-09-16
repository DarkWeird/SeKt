package me.darkweird.sekt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement


const val FRAME_KEY = "frame-075b-4da1-b6ba-e579c2d3230a"
const val WINDOW_KEY = "window-fcc6-11e5-b4f8-330a88ab9d7f"


//region timeouts

@Serializable
data class Timeouts(
    val script: Int? = null,
    val pageLoad: Int? = null,
    val implicit: Int? = null
)

suspend fun Session<*>.getTimeouts(): Timeouts =
    get<Timeouts>("$baseUrl/session/$uuid/timeouts").orThrow()

suspend fun Session<*>.setTimeouts(value: Timeouts) =
    post<Empty?>("$baseUrl/session/$uuid/timeouts", value).orThrow()

//endregion

//region url

@Serializable
class PageUrl(val url: String)

suspend fun Session<*>.getUrl(): WebDriverResult<String> = get("$baseUrl/session/$uuid/url")
suspend fun Session<*>.setUrl(url: String) =
    post<Empty?>("$baseUrl/session/$uuid/url", PageUrl(url)).orThrow()

//endregion

//region back,forward,refresh

suspend fun Session<*>.back() = post<Empty?>("$baseUrl/session/$uuid/back", Empty)
suspend fun <T : WebElement<T>> Session<T>.forward() = post<Empty?>("$baseUrl/session/$uuid/forward", Empty)
suspend fun Session<*>.refresh() = post<Empty?>("$baseUrl/session/$uuid/refresh", Empty)

//endregion

//region title

suspend fun Session<*>.getTitle(): WebDriverResult<String> = get("$baseUrl/session/$uuid/title")

//endRegion

//region window handles

@Serializable
class WindowHandle(val handle: String) // TODO seems there can be another variants

suspend fun Session<*>.getWindowHandle(): WebDriverResult<String> = get("$baseUrl/session/$uuid/window")
suspend fun Session<*>.switchToWindow(window: String): WebDriverResult<Empty?> =
    post("$baseUrl/session/$uuid/window", WindowHandle(window))

suspend fun Session<*>.closeWindow(): WebDriverResult<List<String>> = delete("$baseUrl/session/$uuid/window")
suspend fun Session<*>.getWindowHandles(): WebDriverResult<List<String>> =
    get("$baseUrl/session/$uuid/window/handles")

//endregion

//region frames

@Serializable
sealed class FrameLocator {
    @Serializable
    class IdLocator(val id: Int)

    @Serializable
    class WebElementLocator(val id: String)
}

suspend inline fun <reified T : FrameLocator> Session<*>.switchToFrame(frame: T): WebDriverResult<Empty> =
    post("$baseUrl/session/$uuid/frame", frame)

suspend fun Session<*>.switchToParentFrame(): WebDriverResult<Empty> =
    post("$baseUrl/session/$uuid/frame/parent", Empty)

//endregion

//region window handling

@Serializable
data class Rect<T : Number>(
    val x: T,
    val y: T,
    val width: T,
    val height: T
)

suspend fun Session<*>.getWindowRect(): WebDriverResult<Rect<Int>> = get("$baseUrl/session/$uuid/window/rect")

suspend fun Session<*>.setWindowRect(rect: Rect<Int>): WebDriverResult<Rect<Int>> =
    post(
        "$baseUrl/session/$uuid/window/rect",
        Json.encodeToJsonElement(rect)
    ) // FIXME this is workaround - ktor cannot determinate Rect's serializer

suspend fun Session<*>.windowMaximize(): WebDriverResult<Rect<Int>> =
    post("$baseUrl/session/$uuid/window/maximize", Empty)

suspend fun Session<*>.windowMinimize(): WebDriverResult<Rect<Int>> =
    post("$baseUrl/session/$uuid/window/minimize", Empty)

suspend fun Session<*>.windowFullscreen(): WebDriverResult<Rect<Int>> =
    post("$baseUrl/session/$uuid/window/fullscreen", Empty)

//endregion

//region element

//TODO make W3CWebElement as generic
suspend fun Session<*>.getActiveElement() =
    createWebElement(get("$baseUrl/session/$uuid/element/active"))

@Serializable
data class Locator(
    val using: String,
    val value: String
)

fun css(value: String) = Locator("css selector", value)
fun linkText(value: String) = Locator("link text", value)
fun partialLinkText(value: String) = Locator("partial link text", value)
fun tagName(value: String) = Locator("tag name", value)
fun xpath(value: String) = Locator("xpath", value)

suspend fun <T : WebElement<T>> Session<T>.findElement(locator: Locator) =
    createWebElement(post("$baseUrl/session/$uuid/element", locator))

suspend fun <T : WebElement<T>> Session<T>.findElements(locator: Locator): List<T> =
    post<List<WebElementResponse>>("$baseUrl/session/$uuid/elements", locator).orThrow()
        .map { createWebElement(WebDriverResult.Success(it)) }


suspend inline fun <reified T : WebElement<T>> WebElement<T>.findElement(locator: Locator): WebDriverResult<T> =
    post("$baseUrl/session/$uuid/element/$elementId/element", locator)

suspend inline fun <reified T : WebElement<T>> WebElement<T>.findElements(locator: Locator): WebDriverResult<T> =
    post("$baseUrl/session/$uuid/element/$elementId/elements", locator)

suspend fun WebElement<*>.isSelected(): WebDriverResult<Boolean> =
    get("$baseUrl/session/$uuid/element/$elementId/selected")

suspend fun WebElement<*>.getAttribute(name: String): WebDriverResult<String?> =
    get("$baseUrl/session/$uuid/element/$elementId/attribute/$name")

suspend fun WebElement<*>.getProperty(name: String): WebDriverResult<String?> =
    get("$baseUrl/session/$uuid/element/$elementId/property/$name")

suspend fun WebElement<*>.getCssValue(name: String): WebDriverResult<String> =
    get("$baseUrl/session/$uuid/element/$elementId/css/$name")

suspend fun WebElement<*>.getText(): WebDriverResult<String> = get("$baseUrl/session/$uuid/element/$elementId/text")
suspend fun WebElement<*>.getTagName(): WebDriverResult<String> =
    get("$baseUrl/session/$uuid/element/$elementId/name")

suspend fun WebElement<*>.getRect(): WebDriverResult<Rect<Float>> =
    get("$baseUrl/session/$uuid/element/$elementId/rect")

suspend fun WebElement<*>.isEnabled(): WebDriverResult<Boolean> =
    get("$baseUrl/session/$uuid/element/$elementId/enabled")

suspend fun WebElement<*>.click(): WebDriverResult<Empty?> =
    post("$baseUrl/session/$uuid/element/$elementId/click", Empty)

suspend fun WebElement<*>.clear(): WebDriverResult<Empty?> =
    post("$baseUrl/session/$uuid/element/$elementId/clear", Empty)

@Serializable
class Text(val text: String)

suspend fun WebElement<*>.sendKeys(text: String): WebDriverResult<Empty?> =
    post("$baseUrl/session/$uuid/element/$elementId/value", Text(text))

//endregion

//region Get Page Source

suspend fun Session<*>.getPageSource(): WebDriverResult<String> =
    get("$baseUrl/session/$uuid/source")

//endregion

//region Execute Scripts

@Serializable
data class ScriptData(
    val script: String,
    val args: List<JsonElement>
)

suspend inline fun <reified R> Session<*>.execute(data: ScriptData): WebDriverResult<R> =
    post("$baseUrl/session/$uuid/execute/sync", data)

suspend inline fun <reified R> Session<*>.executeAsync(data: ScriptData): WebDriverResult<R> =
    post("$baseUrl/session/$uuid/execute/async", data)

//endregion


//GET 	/session/{session id}/cookie 	Get All Cookies
//GET 	/session/{session id}/cookie/{name} 	Get Named Cookie
//POST 	/session/{session id}/cookie 	Add Cookie

//region Cookies

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

suspend fun Session<*>.cookies(): WebDriverResult<List<Cookie>> =
    get("$baseUrl/session/$uuid/cookie")

suspend fun Session<*>.getCookie(name: String): WebDriverResult<String> =
    get("$baseUrl/session/$uuid/cookie/$name")

suspend fun Session<*>.setCookie(cookie: Cookie): WebDriverResult<Empty?> =
    post("$baseUrl/session/$uuid/cookie", CookieData(cookie))


suspend fun Session<*>.deleteAll(): WebDriverResult<Empty?> =
    delete("$baseUrl/session/$uuid/cookie")

suspend fun Session<*>.deleteCookie(name: String): WebDriverResult<Empty?> =
    delete("$baseUrl/session/$uuid/cookie/$name")

//endregion

//region actions

@Serializable
sealed class Action {
    @Serializable
    @SerialName("key")
    class KeyAction(val id: String, val actions: List<KeyActionItem>) : Action()

    @Serializable
    @SerialName("pointer")
    class PointAction(val id: String) : Action()


    @Serializable
    @SerialName("none")
    class NoneAction(val id: String, val actions: List<GeneralAction>) : Action()
}

@Serializable
sealed class KeyActionItem {

    @Serializable
    @SerialName("keyUp")
    class KeyUpAction(val value: String) : KeyActionItem()

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


suspend fun Session<*>.performActions(actions: Actions): WebDriverResult<Empty?> =
    post("$baseUrl/session/$uuid/actions", actions)

suspend fun Session<*>.releaseActions(): WebDriverResult<Empty?> =
    delete("$baseUrl/session/$uuid/actions")

//endregion

//region alert

suspend fun Session<*>.dismissAlert(): WebDriverResult<Empty?> =
    post("$baseUrl/session/$uuid/alert/dismiss", Empty)

suspend fun Session<*>.accept(): WebDriverResult<Empty?> =
    post("$baseUrl/session/$uuid/alert/accept", Empty)

suspend fun Session<*>.getAlertText(): WebDriverResult<String> =
    get("$baseUrl/session/$uuid/alert/text")

suspend fun Session<*>.sendAlertText(text: String): WebDriverResult<Empty?> =
    post("$baseUrl/session/$uuid/dismiss", Text(text))

//endregion

//region screenshot
suspend fun Session<*>.takeScreenshot(): WebDriverResult<String> =
    get("$baseUrl/session/$uuid/screenshot")

suspend fun WebElement<*>.takeScreenshot(): WebDriverResult<String> =
    get("$baseUrl/session/$uuid/element/$elementId/screenshot")

//endregion


