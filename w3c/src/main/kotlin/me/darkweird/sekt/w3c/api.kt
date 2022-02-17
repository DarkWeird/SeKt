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
    post<Timeouts, Unit>("/timeouts", value)

suspend fun Session.getUrl(): String =
    get("/url")

suspend fun Session.setUrl(url: PageUrl) =
    post<PageUrl, Unit>("/url", url)

suspend fun Session.back() =
    post<Empty, Unit>("/back", Empty)

suspend fun Session.forward() = post<Empty, Unit>("/forward", Empty)

suspend fun Session.refresh() = post<Empty, Unit>("/refresh", Empty)

suspend fun Session.getTitle(): String = get("/title")

suspend fun Session.getWindowHandle(): String = get("/window")


suspend fun Session.switchToWindow(window: WindowHandle) = post<WindowHandle, Unit>("/window", window)

suspend fun Session.closeWindow(): List<String> = delete("/window")

suspend fun Session.getWindowHandles(): List<String> = get("/window/handles")

suspend fun Session.switchToFrame(frame: SwitchToFrame) = post<SwitchToFrame, Unit>("/frame", frame)

suspend fun Session.switchToParentFrame() = post<Empty, Unit>("/frame/parent", Empty)

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

suspend fun Session.setCookie(cookieData: CookieData) = post<CookieData, Unit>("/cookie", cookieData)

suspend fun Session.deleteAllCookie() = delete<Unit>("/cookie")

suspend fun Session.deleteCookie(name: String) = delete<Unit>("/cookie/$name")

suspend fun Session.performActions(actions: Actions) = post<Actions, Unit>("/actions", actions)

suspend fun Session.releaseActions() = delete<Unit>("/actions")

suspend fun Session.dismissAlert() = post<Empty, Unit>("/alert/dismiss", Empty)

suspend fun Session.acceptAlert() = post<Empty, Unit>("/alert/accept", Empty)

suspend fun Session.getAlertText(): String = get("/alert/text")

suspend fun Session.sendAlertText(text: Text) = post<Text, Unit>("/alert/text", text)

suspend fun Session.takeScreenshot(): String = get("/screenshot")

suspend fun Session.close() = delete<Unit>("")

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

suspend fun WebElement.click() = post<Empty, Unit>("/click", Empty)

suspend fun WebElement.clear() = post<Empty, Unit>("/clear", Empty)

suspend fun WebElement.sendKeys(text: Text) = post<Text, Unit>("/value", text)

suspend fun WebElement.takeScreenshot(): String = get("/screenshot")
