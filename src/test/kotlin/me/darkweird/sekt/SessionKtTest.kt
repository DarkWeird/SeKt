package me.darkweird.sekt

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldStartWith
import kotlinx.serialization.json.JsonPrimitive
import me.darkweird.sekt.w3c.*
import me.darkweird.sekt.w3c.W3CCapabilities.browserName
import me.darkweird.sekt.w3c.W3CCapabilities.browserVersion
import me.darkweird.sekt.w3c.W3CCapabilities.platformName

private suspend fun withTestSession(block: suspend KtorW3CSession.() -> Unit) {
    webdriver("http://localhost:4444/wd/hub")
        .session(
            W3CKtor,
            capabilities
            {
                browserName = "firefox"
                platformName = "linux"
                browserVersion = "92.0"
            },
            block
        )
}

class CoreTests : FunSpec() {
    init {
        test("status") {
            val driver = webdriver("http://localhost:4444/wd/hub")
            driver.status().ready shouldBe true
        }

        test("timeouts") {
            withTestSession {
                setTimeouts(
                    Timeouts(
                        pageLoad = 42
                    )
                )

                getTimeouts().pageLoad shouldBe 42
            }
        }

        test("urls") {
            withTestSession {
                setUrl(PageUrl("https://www.google.com/"))
                getUrl() shouldBe "https://www.google.com/"
            }
        }

        test("forward/back/refresh") {
            withTestSession {
                setUrl(PageUrl("https://www.google.com/"))
                getUrl() shouldBe "https://www.google.com/"

                setUrl(PageUrl("https://www.bing.com/"))
                getUrl() shouldBe "https://www.bing.com/"

                back()
                getUrl() shouldBe "https://www.google.com/"

                forward()
                getUrl() shouldBe "https://www.bing.com/"

                refresh()
                getUrl() shouldBe "https://www.bing.com/"
            }
        }
//
        test("title") {
            withTestSession {
                setUrl(PageUrl("https://www.google.com/"))
                getTitle() shouldBe "Google"
            }
        }

        test("window handles") {
            //Closes single window - closes session
            val session = webdriver("http://localhost:4444/wd/hub")
                .session(
                    W3CKtor,
                    capabilities
                    {
                        browserName = "firefox"
                        platformName = "linux"
                        browserVersion = "92.0"
                    })
            session.run {
                setUrl(PageUrl("https://www.google.com/"))
                val handle = getWindowHandle()
                val handles = getWindowHandles()
                handles shouldContain handle
                switchToWindow(WindowHandle(handle))
                closeWindow()
            }
        }

        test("window rectangles") {
            withTestSession {
                setUrl(PageUrl("https://www.google.com/"))
                val rect = getWindowRect()
                windowFullscreen()
                windowMinimize()
                windowMaximize()
                setWindowRect(rect)
            }
        }

        test("find elements") {
            withTestSession {
                setUrl(PageUrl("https://www.google.com/"))
                val byActive = getActiveElement().getRect()
                val bySingleFind = findElement(xpath("//input[@name ='q']")).getRect()
                val byMultipleFind = findElements(xpath("//input[@name ='q']"))[0].getRect()
                bySingleFind shouldBe byActive
                byMultipleFind shouldBe byActive
            }
        }

        test("web elements methods") {
            withTestSession {
                setUrl(PageUrl("https://www.google.com/"))
                getActiveElement().run {
                    click()
                    isEnabled() shouldBe true
                    sendKeys(Text("fuuu"))
                    getProperty("value") shouldBe "fuuu"
                    clear()
                    getProperty("value") shouldBe ""
                    getText() shouldBe ""
                    getTagName() shouldBe "input"
                    getAttribute("name") shouldBe "q"
                    getRect()
                    getCssValue("color") shouldMatch "rgba?\\([0-9, .]+\\)"
                }
                getPageSource() shouldStartWith "<html"
            }

        }

        test("sync javascript execute") {
            withTestSession {
                setUrl(PageUrl("https://www.google.com/"))
                execute(
                    ScriptData(
                        "return document.title",
                        listOf()
                    )
                ) shouldBe JsonPrimitive("Google")


                getActiveElement().sendKeys(Text("Fuuu"))
                execute(
                    ScriptData(
                        "return document.forms[0][arguments[0]].value",
                        listOf(JsonPrimitive("q"))
                    )
                ) shouldBe JsonPrimitive("Fuuu")
            }
        }

        test("actions") {
            withTestSession {
                setUrl(PageUrl("https://www.google.com/"))
                performActions(
                    Actions(
                        listOf(
                            Action.KeyAction(
                                "some",
                                listOf(KeyActionItem.KeyDownAction("f"))
                            )
                        )
                    )
                )
                performActions(
                    Actions(
                        listOf(
                            Action.NoneAction(
                                "pause",
                                listOf(GeneralAction.PauseAction(4000))
                            )
                        )
                    )
                )
            }
        }

        test("screenshots") {
            withTestSession {
                setUrl(PageUrl("https://www.google.com/"))
                takeScreenshot()
                getActiveElement().takeScreenshot()
            }
        }
    }
}
