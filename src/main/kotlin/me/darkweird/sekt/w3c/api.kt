package me.darkweird.sekt.w3c

import kotlinx.serialization.json.JsonElement
import me.darkweird.sekt.SuspendableClosable

interface W3CSession<T> : SuspendableClosable {

    suspend fun getTimeouts(): Timeouts

    suspend fun setTimeouts(value: Timeouts)

    suspend fun getUrl(): String

    suspend fun setUrl(url: PageUrl)

    suspend fun back()

    suspend fun forward()

    suspend fun refresh()

    suspend fun getTitle(): String

    suspend fun getWindowHandle(): String

    suspend fun switchToWindow(window: WindowHandle)

    suspend fun closeWindow(): List<String>
    suspend fun getWindowHandles(): List<String>

    suspend fun switchToFrame(frame: Any)

    suspend fun switchToParentFrame()

    suspend fun getWindowRect(): Rect<Int>

    suspend fun setWindowRect(rect: Rect<Int>): Rect<Int>

    suspend fun windowMaximize(): Rect<Int>

    suspend fun windowMinimize(): Rect<Int>

    suspend fun windowFullscreen(): Rect<Int>

    suspend fun getActiveElement(): W3CElement<T>

    suspend fun findElement(locator: Locator): W3CElement<T>

    suspend fun findElements(locator: Locator): List<W3CElement<T>>

    suspend fun getPageSource(): String

    suspend fun execute(data: ScriptData): JsonElement //TODO make generics

    suspend fun executeAsync(data: ScriptData): JsonElement //TODO make generics

    suspend fun cookies(): List<Cookie>

    suspend fun getCookie(name: String): Cookie

    suspend fun setCookie(cookieData: CookieData)

    suspend fun deleteAllCookie()

    suspend fun deleteCookie(name: String)

    suspend fun performActions(actions: Actions)

    suspend fun releaseActions()

    suspend fun dismissAlert()

    suspend fun acceptAlert()

    suspend fun getAlertText(): String

    suspend fun sendAlertText(text: Text)

    suspend fun takeScreenshot(): String

}

interface W3CElement<T> {

    suspend fun findElement(locator: Locator): W3CElement<T>

    suspend fun findElements(locator: Locator): List<W3CElement<T>>

    suspend fun isSelected(): Boolean

    suspend fun getAttribute(name: String): String?

    suspend fun getProperty(name: String): String?

    suspend fun getCssValue(name: String): String

    suspend fun getText(): String

    suspend fun getTagName(): String

    suspend fun getRect(): Rect<Float>

    suspend fun isEnabled(): Boolean

    suspend fun click()

    suspend fun clear()

    suspend fun sendKeys(text: Text)

    suspend fun takeScreenshot(): String
}