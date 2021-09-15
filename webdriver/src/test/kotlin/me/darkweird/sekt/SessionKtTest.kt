package me.darkweird.sekt

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Disabled
import kotlin.test.*

class TestWebDriver(
    override val baseUrl: String,
    override val createSession: (
        WebDriverResult<CreateSessionResponse>
    ) -> TestSession
) : WebDriver<TestSession, TestElement>

class TestSession(
    override val baseUrl: String, override val uuid: String,
    override val createWebElement: (WebDriverResult<WebElementResponse>) -> TestElement
) : Session<TestElement>

class TestElement(override val baseUrl: String, override val uuid: String, override val elementId: String) :
    WebElement<TestElement>

fun createTestWebDriver(baseUrl: String): TestWebDriver {
    return TestWebDriver(baseUrl) {
        val uuid = it.orThrow().sessionId
        TestSession(baseUrl, uuid) {
            val elementId = it.orThrow().elementId
            TestElement(baseUrl, uuid, elementId)
        }
    }
}

fun withTestSession(code: suspend TestSession.() -> Unit) {
    val driver = createTestWebDriver("http://localhost:4444/wd/hub")
    runBlocking {
        val session = driver.create(
            WebDriverNewSessionParameters(
                WebDriverCapabilities(
                    JsonObject(
                        mapOf(
                            "browserName" to JsonPrimitive("firefox"),
                            "browserVersion" to JsonPrimitive("91.0"),
                            "platformName" to JsonPrimitive("linux"),
                        )
                    )
                )
            )
        )
        try {
            code(session)
        } finally {
            driver.deleteSession(session.uuid)
        }
    }
}

class CoreTests {
    @Test
    fun status() {
        val driver = createTestWebDriver("http://localhost:4444/wd/hub")
        runBlocking { driver.status() }

    }

    @Test
    fun createSessionLegacyCaps() {
        val driver = createTestWebDriver("http://localhost:4444/wd/hub")
        runBlocking {
            val session = driver.create(
                LegacyNewSessionParameters(
                    JsonObject(
                        mapOf(
                            "browserName" to JsonPrimitive("firefox"),
                            "platformName" to JsonPrimitive("linux")
                        )
                    )
                )
            )
            driver.deleteSession(session.uuid)
        }
    }

    @Test
    fun createSessionWebDriverCaps() {
        val driver = createTestWebDriver("http://localhost:4444/wd/hub")
        runBlocking {
            val session = driver.create(
                WebDriverNewSessionParameters(
                    WebDriverCapabilities(
                        JsonObject(
                            mapOf(
                                "browserName" to JsonPrimitive("firefox"),
                            )
                        )
                    )
                )
            )
            driver.deleteSession(session.uuid)
        }
    }

    @Test
    fun timeouts() {

        withTestSession {
            setTimeouts(
                Timeouts(
                    pageLoad = 42
                )
            )

            assertEquals(getTimeouts().pageLoad, 42)
        }
    }

    @Test
    fun urls() {
        withTestSession {
            setUrl("https://www.google.com/")
            assertEquals(getUrl().getOrNull(), "https://www.google.com/")
        }
    }

    @Test
    fun backForwardAndRefresh() {
        withTestSession {
            setUrl("https://www.google.com/")
            assertEquals(getUrl().getOrNull(), "https://www.google.com/")

            setUrl("https://www.bing.com/")
            assertEquals(getUrl().getOrNull(), "https://www.bing.com/")

            back()
            assertEquals(getUrl().getOrNull(), "https://www.google.com/")

            forward()
            assertEquals(getUrl().getOrNull(), "https://www.bing.com/")

            refresh()
            assertEquals(getUrl().getOrNull(), "https://www.bing.com/")
        }
    }

    @Test
    fun title() {
        withTestSession {
            setUrl("https://www.google.com/")
            assertEquals(getTitle().orThrow(), "Google")
        }
    }

    @Test
    fun windowHandle() {
        withTestSession {
            setUrl("https://www.google.com/")
            val handle = getWindowHandle().orThrow();
            val handles = getWindowHandles().orThrow();
            assertContains(handles, handle)
            switchToWindow(handle).orThrow()
            closeWindow().orThrow()
        }
    }

    @Test
    @Disabled("not implemented")
    fun frames() {
        TODO("needs to find site with frames")
    }

    @Test
    fun windowRects() {
        withTestSession {
            setUrl("https://www.google.com/")
            val rect = getWindowRect().orThrow()
            windowFullscreen().orThrow()
            windowMinimize().orThrow()
            windowMaximize().orThrow()
            val restoredRect = setWindowRect(rect).orThrow();
            assertEquals(rect, restoredRect)
        }
    }

    @Test
    fun element() {
        withTestSession {
            setUrl("https://www.google.com/")
            val byActive = getActiveElement().elementId
            val bySingleFind = findElement(xpath("//input[@name ='q']")).elementId
            val byMultipleFind = findElements(xpath("//input[@name ='q']"))[0].elementId
            assertEquals(byActive, bySingleFind)
            assertEquals(byActive, byMultipleFind)
        }
    }

    @Test
    fun elementActions() {
        withTestSession {
            setUrl("https://www.google.com/")
            getActiveElement().run {
                click().orThrow()
                assertTrue { isEnabled().orThrow() }
                sendKeys("fuuu").orThrow()
                assertEquals("fuuu", getProperty("value").orThrow())
                clear()
                assertEquals("", getProperty("value").orThrow())
                assertEquals("", getText().orThrow())
                assertEquals("input", getTagName().orThrow())
                assertEquals("q", getAttribute("name").orThrow())
                getRect().orThrow()
                assertEquals("rgba(0, 0, 0, 0.87)", getCssValue("color").orThrow())
            }
            assertNotNull(getPageSource().orThrow())
        }

    }


    @Test
    fun scriptsSync() {
        withTestSession {
            setUrl("https://www.google.com/")
            assertEquals(
                "Google", execute<String>(
                    ScriptData(
                        "return document.title",
                        listOf()
                    )
                ).orThrow()
            )

            getActiveElement().sendKeys("Fuuu")
            assertEquals(
                "Fuuu", execute<String?>(
                    ScriptData(
                        "return document.forms[0][arguments[0]].value",
                        listOf(JsonPrimitive("q"))
                    )
                ).orThrow()
            )
        }
    }

    @Test
    @Disabled("make script")
    fun scriptAsync() {
        withTestSession {
            executeAsync<String>(
                ScriptData(
                    "document.title",
                    listOf()
                )
            ).orThrow()
        }
    }

    @Test
    @Disabled("invalid cookie domain : Document is cookie-averse")
    fun cookies() {
        withTestSession {

        }
    }

    @Test
    fun actions() {
        withTestSession {
            setUrl("https://www.google.com/")
            performActions(
                Actions(
                    listOf(
                        Action.KeyAction(
                            "some",
                            listOf(KeyActionItem.KeyDownAction("f"))
                        )
                    )
                )
            ).orThrow()
            performActions(
                Actions(
                    listOf(
                        Action.NoneAction(
                            "pause",
                            listOf(GeneralAction.PauseAction(4000))
                        )
                    )
                )
            ).orThrow()
        }
    }

    @Test
    fun screenshots() {
        withTestSession {
            setUrl("https://www.google.com/")
            takeScreenshot().orThrow()
            getActiveElement().takeScreenshot()
        }
    }
}