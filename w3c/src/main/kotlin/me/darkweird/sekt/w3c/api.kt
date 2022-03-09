package me.darkweird.sekt.w3c

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import me.darkweird.sekt.core.*

object DefaultSessionCreator : SessionFactory<Session> {
    override suspend fun create(driver: WebDriver, capabilities: WebDriverNewSessionParameters): Session {
        val params = driver.post<WebDriverNewSessionParameters, CreateSessionResponse>("/session", capabilities)
        return Session(params.sessionId, driver)
    }
}

suspend fun WebDriver.status(): Status = get("/status")

suspend fun Session.getTimeouts(): Timeouts = get("/timeouts")

suspend fun Session.setTimeouts(value: Timeouts) =
    post<Timeouts, Empty?>("/timeouts", value)

suspend fun Session.getUrl(): String =
    get("/url")

suspend fun Session.setUrl(url: PageUrl) =
    post<PageUrl, Empty?>("/url", url)

suspend fun Session.back() =
    post<Empty, Empty?>("/back", Empty)

suspend fun Session.forward() = post<Empty, Empty?>("/forward", Empty)

suspend fun Session.refresh() = post<Empty, Empty?>("/refresh", Empty)

suspend fun Session.getTitle(): String = get("/title")

suspend fun Session.getWindowHandle(): String = get("/window")


suspend fun Session.switchToWindow(window: WindowHandle) = post<WindowHandle, Empty?>("/window", window)

suspend fun Session.closeWindow(): List<String> = delete("/window")

suspend fun Session.getWindowHandles(): List<String> = get("/window/handles")

suspend fun Session.switchToFrame(frame: SwitchToFrame) = post<SwitchToFrame, Empty?>("/frame", frame)

suspend fun Session.switchToParentFrame() = post<Empty, Empty?>("/frame/parent", Empty)

suspend fun Session.getWindowRect(): Rect<Int> = get("/window/rect")

suspend fun Session.setWindowRect(rect: Rect<Int>): Rect<Int> =
    post("/window/rect", Json.encodeToJsonElement(rect))

suspend fun Session.windowMaximize(): Rect<Int> = post("/window/maximize", Empty)

suspend fun Session.windowMinimize(): Rect<Int> = post("/window/minimize", Empty)

suspend fun Session.windowFullscreen(): Rect<Int> = post("/window/fullscreen", Empty)

suspend fun Session.getActiveElement(): WebElement =
    WebElement(
        get<WebElementObject>("/element/active").elementId,
        this
    )
    { elementId = session.get<WebElementObject>("/element/active").elementId }


suspend fun Session.findElement(locator: Locator): WebElement =
    WebElement(
        post<Locator, WebElementObject>("/element", locator).elementId,
        this
    ) { elementId = post<Locator, WebElementObject>("/element", locator).elementId }


suspend fun Session.findElements(locator: Locator): List<WebElement> =
    post<Locator, List<WebElementObject>>("/elements", locator).map {
        WebElement(
            it.elementId,
            this
        ) { TODO("Implement re search elementId for collections") }
    }


suspend fun Session.getPageSource(): String = get("/source")

@JvmName("execute")
suspend inline fun <reified R> Session.execute(data: ScriptData): R = post("/execute/sync", data)

@JvmName("executeVoid")
suspend fun Session.execute(data: ScriptData) = post<ScriptData, Empty>("/execute/sync", data)

@JvmName("executeAsync")
suspend inline fun <reified R> Session.executeAsync(data: ScriptData): R = post("/execute/async", data)

@JvmName("executeAsyncVoid")
suspend fun Session.executeAsync(data: ScriptData): Empty = post("/execute/async", data)

suspend fun Session.cookies(): List<Cookie> = get("/cookie")

suspend fun Session.getCookie(name: String): Cookie = get("/cookie/$name")

suspend fun Session.setCookie(cookieData: CookieData) = post<CookieData, Empty?>("/cookie", cookieData)

suspend fun Session.deleteAllCookie() = delete<Empty?>("/cookie")

suspend fun Session.deleteCookie(name: String) = delete<Empty?>("/cookie/$name")

suspend fun Session.performActions(actions: Actions) = post<Actions, Empty?>("/actions", actions)

suspend fun Session.releaseActions() = delete<Empty?>("/actions")

suspend fun Session.dismissAlert() = post<Empty, Empty?>("/alert/dismiss", Empty)

suspend fun Session.acceptAlert() = post<Empty, Empty?>("/alert/accept", Empty)

suspend fun Session.getAlertText(): String = get("/alert/text")

suspend fun Session.sendAlertText(text: Text) = post<Text, Empty?>("/alert/text", text)

suspend fun Session.takeScreenshot(): String = get("/screenshot")

suspend fun Session.close() = delete<Empty?>("")

suspend fun WebElement.findElement(locator: Locator): WebElement =
    WebElement(
        post<Locator, WebElementObject>("/element", locator).elementId,
        session
    ) {
        elementId = post<Locator, WebElementObject>("/element", locator).elementId
    }


suspend inline fun WebElement.findElements(locator: Locator): List<WebElement> =
    post<Locator, List<WebElementObject>>("/elements", locator).map {
        WebElement(
            it.elementId,
            session
        ) { TODO("Implement re search elementId for collections") }

    }


suspend fun WebElement.isSelected(): Boolean = get("/selected")

suspend fun WebElement.getAttribute(name: String): String? = get("/attribute/$name")

suspend fun WebElement.getProperty(name: String): String? = get("/property/$name")

suspend fun WebElement.getCssValue(name: String): String = get("/css/$name")

suspend fun WebElement.getText(): String = get("/text")

suspend fun WebElement.getTagName(): String = get("/name")

suspend fun WebElement.getRect(): Rect<Float> = get("/rect")

suspend fun WebElement.isEnabled(): Boolean = get("/enabled")

suspend fun WebElement.click() = post<Empty, Empty?>("/click", Empty)

suspend fun WebElement.clear() = post<Empty, Empty?>("/clear", Empty)

suspend fun WebElement.sendKeys(text: Text) = post<Text, Empty?>("/value", text)

suspend fun WebElement.takeScreenshot(): String = get("/screenshot")
