package me.darkweird.sekt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


const val FRAME_KEY = "frame-075b-4da1-b6ba-e579c2d3230a"
const val WINDOW_KEY = "window-fcc6-11e5-b4f8-330a88ab9d7f"


//region timeouts

@Serializable
data class Timeouts(
    val script: Int? = null,
    val pageLoad: Int? = null,
    val implicit: Int? = null
)

suspend fun <T : WebElement<T>> Session<T>.getTimeouts(): Timeouts =
    get<Timeouts>("$baseUrl/session/$uuid/timeouts").orThrow()

suspend fun <T : WebElement<T>> Session<T>.setTimeouts(value: Timeouts) =
    post<Empty?>("$baseUrl/session/$uuid/timeouts", value).orThrow()

//endregion

//region url

@Serializable
class PageUrl(val url: String)

suspend fun <T : WebElement<T>> Session<T>.getUrl(): WebDriverResult<String> = get("$baseUrl/session/$uuid/url")
suspend fun <T : WebElement<T>> Session<T>.setUrl(url: String) =
    post<Empty?>("$baseUrl/session/$uuid/url", PageUrl(url)).orThrow()

//endregion

//region back,forward,refresh

suspend fun <T : WebElement<T>> Session<T>.back() = post<Empty?>("$baseUrl/session/$uuid/back", Empty)
suspend fun <T : WebElement<T>> Session<T>.forward() = post<Empty?>("$baseUrl/session/$uuid/forward", Empty)
suspend fun <T : WebElement<T>> Session<T>.refresh() = post<Empty?>("$baseUrl/session/$uuid/refresh", Empty)

//endregion

//region title

suspend fun <T : WebElement<T>> Session<T>.getTitle() = get<String>("$baseUrl/session/$uuid/title")

//endRegion

//region window handles


@Serializable
class WindowHandle(val handle: String) // TODO seems there can be another varians

suspend fun <T : WebElement<T>> Session<T>.getWindowHandle() = get<String>("$baseUrl/session/$uuid/window")
suspend fun <T : WebElement<T>> Session<T>.switchToWindow(window: String) =
    post<Empty?>("$baseUrl/session/$uuid/window", WindowHandle(window))

suspend fun <T : WebElement<T>> Session<T>.closeWindow() = delete<List<String>>("$baseUrl/session/$uuid/window")
suspend fun <T : WebElement<T>> Session<T>.getWindowHandles() =
    get<List<String>>("$baseUrl/session/$uuid/window/handles")

//endregion

//region frames

sealed class FrameLocator {
    @Serializable
    class IdLocator(val id: Int)

    @Serializable
    class WebElementLocator(val id: String)
}

suspend inline fun <reified T : FrameLocator, E : WebElement<E>> Session<E>.switchToFrame(frame: T) =
    post<Empty>("$baseUrl/session/$uuid/frame", frame)

suspend fun <T : WebElement<T>> Session<T>.switchToParentFrame() =
    post<Empty>("$baseUrl/session/$uuid/frame/parent", Empty)
//endregions

//region window handling

@Serializable
data class Rect<T : Number>(
    val x: T,
    val y: T,
    val width: T,
    val height: T
)

suspend fun <T : WebElement<T>> Session<T>.getWindowRect() = get<Rect<Int>>("$baseUrl/session/$uuid/window/rect")
suspend fun <T : WebElement<T>> Session<T>.setWindowRect(rect: Rect<Int>) =
    post<Rect<Int>>("$baseUrl/session/$uuid/window/rect", rect)

suspend fun <T : WebElement<T>> Session<T>.windowMaximize() =
    post<Rect<Int>>("$baseUrl/session/$uuid/window/maximize", Empty)

suspend fun <T : WebElement<T>> Session<T>.windowMinimize() =
    post<Rect<Int>>("$baseUrl/session/$uuid/window/minimize", Empty)

suspend fun <T : WebElement<T>> Session<T>.windowFullscreen() =
    post<Rect<Int>>("$baseUrl/session/$uuid/window/fullscreen", Empty)


class W3CSession<T : WebElement<T>> {
    suspend fun Session<T>.windowFullscreen() =
        post<Rect<Int>>("$baseUrl/session/$uuid/window/fullscreen", Empty)

    suspend fun Session<T>.findElement(locator: Locator) =
        createWebElement(post("$baseUrl/session/$uuid/element", locator))

}
//endregion


//region element

//TODO make W3CWebElement as generic
suspend fun <T : WebElement<T>> Session<T>.getActiveElement() =
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

suspend fun <T : WebElement<T>> Session<T>.findElements(locator: Locator) =
    post<List<WebElementResponse>>("$baseUrl/session/$uuid/elements", locator).orThrow()
        .map { createWebElement(WebDriverResult.Success(it)) }


suspend inline fun <reified T : WebElement<T>> WebElement<T>.findElement(locator: Locator) =
    post<T>("$baseUrl/session/$uuid/element/$elementId/element", locator)

suspend inline fun <reified T : WebElement<T>> WebElement<T>.findElements(locator: Locator) =
    post<T>("$baseUrl/session/$uuid/element/$elementId/elements", locator)

suspend fun <T : WebElement<T>> WebElement<T>.isSelected() =
    get<Boolean>("$baseUrl/session/$uuid/element/$elementId/selected")

suspend fun <T : WebElement<T>> WebElement<T>.getAttribute(name: String) =
    get<String?>("$baseUrl/session/$uuid/element/$elementId/attribute/$name")

suspend fun <T : WebElement<T>> WebElement<T>.getProperty(name: String) =
    get<String?>("$baseUrl/session/$uuid/element/$elementId/property/$name")

suspend fun <T : WebElement<T>> WebElement<T>.getCssValue(name: String) =
    get<String>("$baseUrl/session/$uuid/element/$elementId/css/$name")

suspend fun <T : WebElement<T>> WebElement<T>.getText() = get<String>("$baseUrl/session/$uuid/element/$elementId/text")
suspend fun <T : WebElement<T>> WebElement<T>.getTagName() =
    get<String>("$baseUrl/session/$uuid/element/$elementId/name")

suspend fun <T : WebElement<T>> WebElement<T>.getRect() =
    get<Rect<Float>>("$baseUrl/session/$uuid/element/$elementId/rect")

suspend fun <T : WebElement<T>> WebElement<T>.isEnabled() =
    get<Boolean>("$baseUrl/session/$uuid/element/$elementId/enabled")

suspend fun <T : WebElement<T>> WebElement<T>.click() =
    post<Empty?>("$baseUrl/session/$uuid/element/$elementId/click", Empty)

suspend fun <T : WebElement<T>> WebElement<T>.clear() =
    post<Empty?>("$baseUrl/session/$uuid/element/$elementId/clear", Empty)

@Serializable
class Text(val text: String)

suspend fun <T : WebElement<T>> WebElement<T>.sendKeys(text: String) =
    post<Empty?>("$baseUrl/session/$uuid/element/$elementId/value", Text(text))

//endregion

//region Get Page Source

suspend fun <T : WebElement<T>> Session<T>.getPageSource() =
    get<String>("$baseUrl/session/$uuid/source")

//endregion

//region Execute Scripts

@Serializable
data class ScriptData(
    val script: String,
    val args: List<JsonElement>
)

suspend inline fun <reified R> Session<*>.execute(data: ScriptData) =
    post<R>("$baseUrl/session/$uuid/execute/sync", data)

suspend inline fun <reified R> Session<*>.executeAsync(data: ScriptData) =
    post<R>("$baseUrl/session/$uuid/execute/async", data)

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

suspend fun Session<*>.cookies() =
    get<List<Cookie>>("$baseUrl/session/$uuid/cookie")

suspend fun Session<*>.getCookie(name: String) =
    get<String>("$baseUrl/session/$uuid/cookie/$name")

suspend fun Session<*>.setCookie(cookie: Cookie) =
    post<Empty?>("$baseUrl/session/$uuid/cookie", CookieData(cookie))


suspend fun Session<*>.deleteAll() =
    delete<Empty?>("$baseUrl/session/$uuid/cookie")

suspend fun Session<*>.deleteCookie(name: String) =
    delete<Empty?>("$baseUrl/session/$uuid/cookie/$name")

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

//GET 	/session/{session id}/screenshot 	Take Screenshot
//GET 	/session/{session id}/element/{element id}/screenshot 	Take Element Screenshot
//endregion


