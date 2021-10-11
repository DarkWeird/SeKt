package me.darkweird.example

import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.engine.cio.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import me.darkweird.sekt.capabilities
import me.darkweird.sekt.common.WebDriverException
import me.darkweird.sekt.session
import me.darkweird.sekt.w3c.*
import me.darkweird.sekt.w3c.W3CCapabilities.browserName
import me.darkweird.sekt.w3c.W3CCapabilities.browserVersion
import me.darkweird.sekt.w3c.W3CCapabilities.platformName
import me.darkweird.sekt.webdriver
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

const val webUrl: String = "http://the-internet.herokuapp.com"
const val wdUrl: String = "http://localhost:4444/wd/hub"

private suspend fun withTestSession(block: suspend KtorW3CSession.() -> Unit) {
    webdriver(wdUrl, errorConverters = listOf(w3cConverter()), CIO, httpConfig = {
        engine {
            requestTimeout = 60_000
        }
    })
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

@ExperimentalTime
class TheInternetTests : FunSpec() {
    init {
        concurrency = 1
        timeout = 10_000_000

        test("Add/remove elements") {
            withTestSession {
                setUrl(PageUrl("$webUrl/add_remove_elements/"))

                val addButton = findElement(tagName("button"))
                addButton.click()
                findElements(xpath("//button[@class='added-manually']")).size shouldBe 1

                addButton.click()
                findElements(xpath("//button[@class='added-manually']")).size shouldBe 2

                findElements(xpath("//button[@class='added-manually']"))[0].click()
                findElements(xpath("//button[@class='added-manually']")).size shouldBe 1
            }
        }

        xtest("Basic auth") {
            withTestSession {
                setUrl(PageUrl("$webUrl/basic_auth"))
            }
        }

        xtest("Broken images") {
            withTestSession {
                setUrl(PageUrl("$webUrl/broken_images"))
            }
        }

        xtest("Challenging DOM") {
            withTestSession {
                setUrl(PageUrl("$webUrl/challenging_dom"))
            }
        }

        test("Checkboxes") {
            withTestSession {
                setUrl(PageUrl("$webUrl/checkboxes"))

                val checkboxes = findElements(xpath("//form[@id='checkboxes']/input"))

                checkboxes[0].isSelected() shouldBe false
                checkboxes[1].isSelected() shouldBe true

                checkboxes[0].click()
                checkboxes[0].isSelected() shouldBe true
            }
        }

        test("Context Menu") {
            withTestSession {
                setUrl(PageUrl("$webUrl/context_menu"))

                val contextMenuTarget = findElement(css("#hot-spot"))

                performActions(
                    Actions(
                        listOf(
                            Action.PointerAction(
                                "context_menu",
                                listOf(
                                    PointerActionItem.Move(
                                        100,
                                        Origin.WebElement(WebElementObject(contextMenuTarget.elementId)),
                                        0,
                                        0
                                    ),
                                    PointerActionItem.Down(2),
                                    PointerActionItem.Up(2)
                                )
                            )
                        )
                    )
                )
                getAlertText() shouldBe "You selected a context menu"
                acceptAlert()
            }
        }

        xtest("Digest Authentication") {
            withTestSession {
                setUrl(PageUrl("$webUrl/"))

            }
        }


        xtest("Disappearing Elements") {
            withTestSession {
                setUrl(PageUrl("$webUrl/disappearing_elements"))

            }
        }

        test("Drag and Drop") {
            withTestSession {
                setUrl(PageUrl("$webUrl/drag_and_drop"))

                val (firstColumn, secondColumn) = findElement(css("#columns")).findElements(tagName("div"))
                firstColumn.getText() shouldBe "A"
                secondColumn.getText() shouldBe "B"

                val selenideDragAndDropScript = """
                    function createEvent(typeOfEvent) {
                      var event = document.createEvent("CustomEvent");
                      event.initCustomEvent(typeOfEvent, true, true, null);
                      event.dataTransfer = {
                        data: {},
                        setData: function (key, value) {
                          this.data[key] = value;
                        },
                        getData: function (key) {
                          return this.data[key];
                        }
                      };
                      return event;
                    }

                    function dispatchEvent(element, event, transferData) {
                      if (transferData !== undefined) {
                        event.dataTransfer = transferData;
                      }
                      if (element.dispatchEvent) {
                        element.dispatchEvent(event);
                      } else if (element.fireEvent) {
                        element.fireEvent("on" + event.type, event);
                      }
                    }

                    function dragAndDrop(element, target) {
                      var dragStartEvent = createEvent('dragstart');
                      dispatchEvent(element, dragStartEvent);
                      var dropEvent = createEvent('drop');
                      dispatchEvent(target, dropEvent, dragStartEvent.dataTransfer);
                      var dragEndEvent = createEvent('dragend');
                      dispatchEvent(element, dragEndEvent, dropEvent.dataTransfer);
                    }
                """.trimIndent()
                execute(
                    ScriptData(
                        "$selenideDragAndDropScript; dragAndDrop(arguments[0], arguments[1])",
                        listOf(
                            Json.encodeToJsonElement(WebElementObject(firstColumn.elementId)),
                            Json.encodeToJsonElement(WebElementObject(secondColumn.elementId))
                        )
                    )
                )
                firstColumn.getText() shouldBe "B"
                secondColumn.getText() shouldBe "A"
            }
        }

        test("Dropdown List") {
            withTestSession {
                setUrl(PageUrl("$webUrl/dropdown"))
                val select = findElement(css("#dropdown"))
                select.getProperty("value") shouldBe ""

                select.click()
                val options = select.findElements(tagName("option"))

                options[1].click()
                select.getProperty("value") shouldBe options[1].getProperty("value")
            }
        }

        xtest("Dynamic Content") {
            withTestSession {
                setUrl(PageUrl("$webUrl/dynamic_content"))

            }
        }

        test("Dynamic Controls") {
            withTestSession {
                setUrl(PageUrl("$webUrl/dynamic_controls"))

                // Checkbox check
                val checkbox = findElement(css("#checkbox-example div input"))
                checkbox.click()
                findElement(css("#checkbox-example button")).click()
                checkbox.waitUntil(timeout = Duration.seconds(10)) {
                    val exception = kotlin.runCatching {
                        this.getProperty("value") // just try any endpoint.
                    }.exceptionOrNull() as? WebDriverException
                    exception?.kind == W3CError.NO_SUCH_ELEMENT
                }

                //input disable/enable check
                val input = findElement(css("#input-example input"))
                val switchBtn = findElement(css("#input-example button"))
                input.getAttribute("disabled") shouldBe "true"
                switchBtn.click()
                input.waitUntil {
                    getAttribute("disabled") == null
                }
                input.getAttribute("disabled") shouldBe null
            }
        }

        test("Dynamic Loading/1") {
            withTestSession {
                setUrl(PageUrl("$webUrl/dynamic_loading"))

                findElement(linkText("Example 1: Element on page that is hidden")).click()

                findElement(css("#start button")).click()
                findElement(css("#start")).getCssValue("display") shouldBe "none"

                val loading = findElement(css("#loading"))
                loading.getCssValue("display") shouldBe "block"
                loading.waitUntil { getCssValue("display") == "none" }
                loading.getCssValue("display") shouldBe "none"

                findElement(css("#finish")).getText() shouldBe "Hello World!"
            }
        }

        test("Dynamic Loading/2") {
            withTestSession {
                setUrl(PageUrl("$webUrl/dynamic_loading"))

                findElement(linkText("Example 1: Element on page that is hidden")).click()

                val finishText = findElement(css("#finish"))

                findElement(css("#start button")).click()
                findElement(css("#start")).getCssValue("display") shouldBe "none"

                val loading = findElement(css("#loading"))
                loading.getCssValue("display") shouldBe "block"
                loading.waitUntil { getCssValue("display") == "none" }
                loading.getCssValue("display") shouldBe "none"

                finishText.getText() shouldBe "Hello World!"
            }
        }

        xtest("Entry Ad") {
            withTestSession {
                setUrl(PageUrl("$webUrl/entry_ad"))

            }
        }

        xtest("Exit Intent") {
            withTestSession {
                setUrl(PageUrl("$webUrl/exit_intent"))

            }
        }

        xtest("File Download") {
            withTestSession {
                setUrl(PageUrl("$webUrl/download"))

            }
        }

        xtest("File Unload") {
            withTestSession {
                setUrl(PageUrl("$webUrl/upload"))

            }
        }

        xtest("Floating menu") {
            withTestSession {
                setUrl(PageUrl("$webUrl/floating_menu"))

            }
        }

        xtest("Forgot Password") {
            withTestSession {
                setUrl(PageUrl("$webUrl/forgot_password"))


            }
        }

        test("Form Authentication/1") {
            withTestSession {
                setUrl(PageUrl("$webUrl/login"))

                findElement(css("#username")).sendKeys(Text("tomsmith"))
                findElement(css("#password")).sendKeys(Text("SuperSecretPassword!"))
                findElement(css("#login button")).click()

                findElement(css("#flash")).getText() shouldContain "You logged into a secure area!"
            }
        }

        test("Form Authentication/2") {
            withTestSession {
                setUrl(PageUrl("$webUrl/login"))

                findElement(css("#login button")).click()

                findElement(css("#flash")).getText() shouldContain "Your username is invalid!"
            }
        }

        test("Form Authentication/3") {
            withTestSession {
                setUrl(PageUrl("$webUrl/login"))

                findElement(css("#username")).sendKeys(Text("tomsmith"))
                findElement(css("#login button")).click()

                findElement(css("#flash")).getText() shouldContain "Your password is invalid!"
            }
        }

        test("Nested frames") {
            withTestSession {
                setUrl(PageUrl("$webUrl/nested_frames"))

                runCatching { findElement(css("frameset")) }.shouldBeSuccess()

                run { // go to top frame
                    val frameElement = findElement(xpath("//frame[@name = 'frame-top']"))
                    switchToFrame(SwitchToFrame.WebElement(frameElement))
                    runCatching { findElement(css("frameset")) }.shouldBeSuccess()
                }

                run { // go deeper to right
                    val frameElement = findElement(xpath("//frame[@name = 'frame-right']"))
                    switchToFrame(SwitchToFrame.WebElement(frameElement))
                    findElement(css("body")).getText() shouldBe "RIGHT"
                }

                switchToParentFrame() // returns to top
                runCatching { findElement(css("frameset")) }.shouldBeSuccess()

                run { // go deeper to left
                    val frameElement = findElement(xpath("//frame[@name = 'frame-left']"))
                    switchToFrame(SwitchToFrame.WebElement(frameElement))
                    findElement(css("body")).getText() shouldBe "LEFT"
                }

                switchToFrame(SwitchToFrame.Null()) // returns to top
                runCatching { findElement(css("#content")) }.shouldBeFailure()

            }
        }

        test("iframes") {
            withTestSession {
                setUrl(PageUrl("$webUrl/iframe"))

                runCatching { findElement(css("#tinymce")) }.shouldBeFailure()
                switchToFrame(SwitchToFrame.Number(0))

                findElement(css("#tinymce")).getText() shouldBe "Your content goes here."
            }
        }

        xtest("Geolocation") {
            withTestSession {
                setUrl(PageUrl("$webUrl/geolocation"))
            }
        }

        test("Horizontal Slider") {
            withTestSession {
                setUrl(PageUrl("$webUrl/horizontal_slider"))
                val slider = findElement(tagName("input"))
                val rangeValue = findElement(css("#range"))
                rangeValue.getText() shouldBe "0"
                slider.getProperty("value") shouldBe "0"
                slider.click()
                performActions(
                    Actions(
                        listOf(
                            Action.KeyAction(
                                "id",
                                listOf( // Just go to end of slider, then one step to left
                                    KeyActionItem.KeyDownAction("\uE014"),// Right
                                    KeyActionItem.KeyUpAction("\uE014"),
                                    KeyActionItem.KeyDownAction("\uE014"),// Right
                                    KeyActionItem.KeyUpAction("\uE014"),
                                    KeyActionItem.KeyDownAction("\uE014"),// Right
                                    KeyActionItem.KeyUpAction("\uE014"),
                                    KeyActionItem.KeyDownAction("\uE014"),// Right
                                    KeyActionItem.KeyUpAction("\uE014"),
                                    KeyActionItem.KeyDownAction("\uE014"),// Right
                                    KeyActionItem.KeyUpAction("\uE014"),
                                    KeyActionItem.KeyDownAction("\uE014"),// Right
                                    KeyActionItem.KeyUpAction("\uE014"),

                                    KeyActionItem.KeyDownAction("\uE012"),// Left
                                    KeyActionItem.KeyUpAction("\uE012")
                                )
                            )
                        )
                    )
                )
                rangeValue.getText() shouldBe "4.5"
            }
        }

        test("Hovers") {
            withTestSession {
                setUrl(PageUrl("$webUrl/hovers"))
                val figures = findElements(css(".figure"))
                performActions(
                    Actions(
                        listOf(
                            Action.PointerAction(
                                "move",
                                listOf(
                                    PointerActionItem.Move(
                                        100,
                                        Origin.WebElement(WebElementObject(figures[0].elementId)),
                                        0,
                                        0
                                    )
                                )
                            )
                        )
                    )
                )
                figures[0].findElement(css("div h5")).asClue {
                    it.getText() shouldBe "name: user1"
                }
            }
        }

        xtest("Infinity Scroll") {
            withTestSession {
                setUrl(PageUrl("$webUrl/infinite_scroll"))


            }
        }

        xtest("Inputs") {
            withTestSession {
                setUrl(PageUrl("$webUrl/inputs"))

            }
        }

        test("Windows") {
            withTestSession {
                setUrl(PageUrl("$webUrl/windows"))
                findElement(linkText("Click Here")).click()
                switchToWindow(WindowHandle((getWindowHandles() - getWindowHandle()).first()))
                waitUntil { runCatching { findElement(css("h3")) }.isSuccess }
                findElement(css("h3")).getText() shouldBe "New Window"
            }
        }

        xtest("Notification Message") {
            withTestSession {
                setUrl(PageUrl("$webUrl/notification_message_rendered"))


            }
        }

        xtest("Redirections") {
            withTestSession {
                setUrl(PageUrl("$webUrl/redirector"))


            }
        }

        xtest("Secure File Download") {
            withTestSession {
                setUrl(PageUrl("$webUrl/download-secure"))


            }
        }

        xtest("Shadow DOM") {
            withTestSession {
                setUrl(PageUrl("$webUrl/shadow_dom"))


            }
        }
    }
}
