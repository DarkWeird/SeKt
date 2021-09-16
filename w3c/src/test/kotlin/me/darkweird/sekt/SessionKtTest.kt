package me.darkweird.sekt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldStartWith
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

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

suspend fun createTestWebDriver(baseUrl: String): TestWebDriver {
    return TestWebDriver(baseUrl) {
        val uuid = it.orThrow().sessionId
        TestSession(baseUrl, uuid) {
            val elementId = it.orThrow().elementId
            TestElement(baseUrl, uuid, elementId)
        }
    }
}

suspend fun withTestSession(code: suspend TestSession.() -> Unit) {
    val driver = createTestWebDriver("http://localhost:4444/wd/hub")
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

class CoreTests : FunSpec() {
    init {
        test("status") {
            val driver = createTestWebDriver("http://localhost:4444/wd/hub")
            driver.status()
        }

        test("session legacy") {
            val driver = createTestWebDriver("http://localhost:4444/wd/hub")

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

        test("session w3c") {
            val driver = createTestWebDriver("http://localhost:4444/wd/hub")

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

        test("timeouts") {

            withTestSession {
                setTimeouts(
                    Timeouts(
                        pageLoad = 42
                    )
                )

                42 shouldBe getTimeouts().pageLoad
            }
        }

        test("urls") {
            withTestSession {
                setUrl("https://www.google.com/")
                getUrl().getOrNull() shouldBe "https://www.google.com/"
            }
        }

        test("forward/back/refresh") {
            withTestSession {
                setUrl("https://www.google.com/")
                getUrl().getOrNull() shouldBe "https://www.google.com/"

                setUrl("https://www.bing.com/")
                getUrl().getOrNull() shouldBe "https://www.bing.com/"

                back()
                getUrl().getOrNull() shouldBe "https://www.google.com/"

                forward()
                getUrl().getOrNull() shouldBe "https://www.bing.com/"

                refresh()
                getUrl().getOrNull() shouldBe "https://www.bing.com/"
            }
        }

        test("title") {
            withTestSession {
                setUrl("https://www.google.com/")
                getTitle().orThrow() shouldBe "Google"
            }
        }

        test("window handles") {
            withTestSession {
                setUrl("https://www.google.com/")
                val handle = getWindowHandle().orThrow();
                val handles = getWindowHandles().orThrow();
                handles shouldContain handle
                switchToWindow(handle).orThrow()
                closeWindow().orThrow()
            }
        }

        test("window rectangles") {
            withTestSession {
                setUrl("https://www.google.com/")
                val rect = getWindowRect().orThrow()
                windowFullscreen().orThrow()
                windowMinimize().orThrow()
                windowMaximize().orThrow()
                val restoredRect = setWindowRect(rect).orThrow();
                restoredRect shouldBe rect
            }
        }

        test("find elements") {
            withTestSession {
                setUrl("https://www.google.com/")
                val byActive = getActiveElement().elementId
                val bySingleFind = findElement(xpath("//input[@name ='q']")).elementId
                val byMultipleFind = findElements(xpath("//input[@name ='q']"))[0].elementId
                bySingleFind shouldBe byActive
                byMultipleFind shouldBe byActive
            }
        }

        test("web elements methods") {
            withTestSession {
                setUrl("https://www.google.com/")
                getActiveElement().run {
                    click().orThrow()
                    isEnabled().orThrow() shouldBe true
                    sendKeys("fuuu").orThrow()
                    getProperty("value").orThrow() shouldBe "fuuu"
                    clear()
                    getProperty("value").orThrow() shouldBe ""
                    getText().orThrow() shouldBe ""
                    getTagName().orThrow() shouldBe "input"
                    getAttribute("name").orThrow() shouldBe "q"
                    getRect().orThrow()
                    getCssValue("color").orThrow() shouldMatch "rgba\\([0-9, .]+\\)"
                }
                getPageSource().orThrow() shouldStartWith "<html"
            }

        }

        test("sync javascript execute") {
            withTestSession {
                setUrl("https://www.google.com/")
                execute<String>(
                    ScriptData(
                        "return document.title",
                        listOf()
                    )
                ).orThrow() shouldBe "Google"


                getActiveElement().sendKeys("Fuuu")
                execute<String?>(
                    ScriptData(
                        "return document.forms[0][arguments[0]].value",
                        listOf(JsonPrimitive("q"))
                    )
                ).orThrow() shouldBe "Fuuu"
            }
        }

        test("actions") {
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

        test("screenshots") {
            withTestSession {
                setUrl("https://www.google.com/")
                takeScreenshot().orThrow()
                getActiveElement().takeScreenshot()
            }
        }
    }
}