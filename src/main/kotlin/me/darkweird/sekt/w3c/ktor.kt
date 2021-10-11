package me.darkweird.sekt.w3c

import io.ktor.client.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import me.darkweird.sekt.*

object W3CKtor : SessionFactory<WebDriver<HttpClient>, KtorW3CSession> {
    override suspend fun create(
        driver: WebDriver<HttpClient>,
        capabilities: WebDriverNewSessionParameters
    ): KtorW3CSession {
        val params = driver.post<WebDriverNewSessionParameters, CreateSessionResponse>("/session", capabilities)
        return KtorW3CSession(Session(params.sessionId, driver))
    }
}

suspend fun WebDriver<HttpClient>.status(): Status = get("/status")

class KtorW3CSession(private val session: Session<HttpClient>) : W3CSession<HttpClient> {

    override val sessionId: String = session.sessionId

    override suspend fun getTimeouts(): Timeouts = session.get("/timeouts")

    override suspend fun setTimeouts(value: Timeouts) =
        session.post<Timeouts, Unit>("/timeouts", value)

    override suspend fun getUrl(): String =
        session.get("/url")

    override suspend fun setUrl(url: PageUrl) =
        session.post<PageUrl, Unit>("/url", url)

    override suspend fun back() =
        session.post<Empty, Unit>("/back", Empty)

    override suspend fun forward() = session.post<Empty, Unit>("/forward", Empty)

    override suspend fun refresh() = session.post<Empty, Unit>("/refresh", Empty)

    override suspend fun getTitle(): String = session.get("/title")

    override suspend fun getWindowHandle(): String = session.get("/window")


    override suspend fun switchToWindow(window: WindowHandle) = session.post<WindowHandle, Unit>("/window", window)

    override suspend fun closeWindow(): List<String> = session.delete("/window")

    override suspend fun getWindowHandles(): List<String> = session.get("/window/handles")

    override suspend fun switchToFrame(frame: SwitchToFrame) = session.post<SwitchToFrame, Unit>("/frame", frame)

    override suspend fun switchToParentFrame() = session.post<Empty, Unit>("/frame/parent", Empty)

    override suspend fun getWindowRect(): Rect<Int> = session.get("/window/rect")

    override suspend fun setWindowRect(rect: Rect<Int>): Rect<Int> =
        session.post("/window/rect", Json.encodeToJsonElement(rect))

    override suspend fun windowMaximize(): Rect<Int> = session.post("/window/maximize", Empty)

    override suspend fun windowMinimize(): Rect<Int> = session.post("/window/minimize", Empty)

    override suspend fun windowFullscreen(): Rect<Int> = session.post("/window/fullscreen", Empty)

    override suspend fun getActiveElement(): W3CElement<HttpClient> =
        KtorW3CWebElement(
            WebElement(
                session.get<WebElementObject>("/element/active").elementId,
                session
            )
            { elementId = session.get<WebElementObject>("/element/active").elementId }
        )

    override suspend fun findElement(locator: Locator): W3CElement<HttpClient> =
        KtorW3CWebElement(
            WebElement(
                session.post<Locator, WebElementObject>("/element", locator).elementId,
                session
            ) { elementId = session.post<Locator, WebElementObject>("/element", locator).elementId }
        )

    override suspend fun findElements(locator: Locator): List<W3CElement<HttpClient>> =
        session.post<Locator, List<WebElementObject>>("/elements", locator).map {
            KtorW3CWebElement(
                WebElement(
                    it.elementId,
                    session
                ) { TODO("Implement re search elementId for collections") }
            )
        }


    override suspend fun getPageSource(): String = session.get("/source")

    override suspend fun execute(data: ScriptData): JsonElement = session.post("/execute/sync", data)

    override suspend fun executeAsync(data: ScriptData): JsonElement = session.post("/execute/async", data)

    override suspend fun cookies(): List<Cookie> = session.get("/cookie")

    override suspend fun getCookie(name: String): Cookie = session.get("/cookie/$name")

    override suspend fun setCookie(cookieData: CookieData) = session.post<CookieData, Unit>("/cookie", cookieData)

    override suspend fun deleteAllCookie() = session.delete<Unit>("/cookie")

    override suspend fun deleteCookie(name: String) = session.delete<Unit>("/cookie/$name")

    override suspend fun performActions(actions: Actions) = session.post<Actions, Unit>("/actions", actions)

    override suspend fun releaseActions() = session.delete<Unit>("/actions")

    override suspend fun dismissAlert() = session.post<Empty, Unit>("/alert/dismiss", Empty)

    override suspend fun acceptAlert() = session.post<Empty, Unit>("/alert/accept", Empty)

    override suspend fun getAlertText(): String = session.get("/alert/text")

    override suspend fun sendAlertText(text: Text) = session.post<Text, Unit>("/alert/text", text)

    override suspend fun takeScreenshot(): String = session.get("/screenshot")

    override suspend fun close() = session.delete<Unit>("")

}

class KtorW3CWebElement(private val element: WebElement<HttpClient>) : W3CElement<HttpClient> {
    override val elementId: String
        get() = element.elementId

    override suspend fun findElement(locator: Locator): W3CElement<HttpClient> =
        KtorW3CWebElement(
            WebElement(
                element.post<Locator, WebElementObject>("/element", locator).elementId,
                element.session
            ) {
                elementId = element.post<Locator, WebElementObject>("/element", locator).elementId
            }
        )

    override suspend fun findElements(locator: Locator): List<W3CElement<HttpClient>> =
        element.post<Locator, List<WebElementObject>>("/elements", locator).map {
            KtorW3CWebElement(
                WebElement(
                    it.elementId,
                    element.session
                ) { TODO("Implement re search elementId for collections") }
            )
        }


    override suspend fun isSelected(): Boolean = element.get("/selected")

    override suspend fun getAttribute(name: String): String? = element.get("/attribute/$name")

    override suspend fun getProperty(name: String): String? = element.get("/property/$name")

    override suspend fun getCssValue(name: String): String = element.get("/css/$name")

    override suspend fun getText(): String = element.get("/text")

    override suspend fun getTagName(): String = element.get("/name")

    override suspend fun getRect(): Rect<Float> = element.get("/rect")

    override suspend fun isEnabled(): Boolean = element.get("/enabled")

    override suspend fun click() = element.post<Empty, Unit>("/click", Empty)

    override suspend fun clear() = element.post<Empty, Unit>("/clear", Empty)

    override suspend fun sendKeys(text: Text) = element.post<Text, Unit>("/value", text)

    override suspend fun takeScreenshot(): String = element.get("/screenshot")
}