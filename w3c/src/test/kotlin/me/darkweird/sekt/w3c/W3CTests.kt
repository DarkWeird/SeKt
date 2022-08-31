package me.darkweird.sekt.w3c

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.Tuple3
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import me.darkweird.sekt.core.*
import me.darkweird.sekt.w3c.W3CCapabilities.browserName
import java.util.*

class W3CTests : FunSpec({

    test("status") {
        val mock = webDriver(
            response(
                HttpMethod.Get,
                "/status",
                buildJsonObject {
                    put("ready", JsonPrimitive(true))
                    put("message", JsonPrimitive("Any"))
                })
        )
        mock.status().asClue {
            it.ready shouldBe true
            it.message shouldBe "Any"
        }
    }

    test("session") {
        val sessionId = UUID.randomUUID().toString()
        mockSession(sessionId).sessionId shouldBe sessionId
    }

    test("getUrl") {
        val value = "http://someurl.ru"

        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Get, sessionurl(sessionId, "url"),
                JsonPrimitive(value)
            ),
        )
        session.getUrl() shouldBe value
    }

    test("setUrl") {
        val value = "http://someurl.ru"

        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "url")) { request ->
                request.asJson() shouldBe buildJsonObject {
                    put("url", value)
                }
                JsonNull
            },
        )
        session.setUrl(PageUrl(value))
    }

    test("getTimeouts") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Get, sessionurl(sessionId, "timeouts"),
                buildJsonObject {
                    put("implicit", 1)
                    put("pageLoad", 2)
                    put("script", 3)
                }
            ),
        )
        session.getTimeouts().asClue {
            it.implicit shouldBe 1
            it.pageLoad shouldBe 2
            it.script shouldBe 3
        }
    }
    test("setTimeouts") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "timeouts")) { request ->
                request.asJson() shouldBe buildJsonObject {
                    put("implicit", 1)
                    put("pageLoad", 2)
                    put("script", 3)
                }
                JsonNull
            },
        )
        session.setTimeouts(
            Timeouts(
                implicit = 1,
                pageLoad = 2,
                script = 3
            )
        )
    }

    test("back") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "back")) { request ->
                request.asJson() shouldBe buildJsonObject { }
                JsonNull
            },
        )
        session.back()
    }

    test("forward") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "forward")) { request ->
                request.asJson() shouldBe buildJsonObject { }
                JsonNull
            },
        )
        session.forward()
    }

    test("refresh") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "refresh")) { request ->
                request.asJson() shouldBe buildJsonObject { }
                JsonNull
            },
        )

        session.refresh()
    }

    test("getTitle") {
        val value = "Some title"
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Get, sessionurl(sessionId, "title"),
                JsonPrimitive(value)
            ),
        )

        session.getTitle() shouldBe value
    }

    test("getWindowHandle") {
        val value = "SomeWindowHandler"
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Get, sessionurl(sessionId, "window"),
                JsonPrimitive(value)
            ),
        )

        session.getWindowHandle() shouldBe value
    }

    test("switchToWindow") {
        val value = "SomeWindowHandler"

        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "window")) { request ->
                request.asJson() shouldBe buildJsonObject {
                    put("handle", value)
                }
                JsonNull
            },
        )
        session.switchToWindow(WindowHandle(value))
    }

    test("closeWindow") {
        val value = listOf("123", "321")

        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Delete, sessionurl(sessionId, "window"),
                buildJsonArray {
                    value.forEach {
                        add(it)
                    }
                }
            ),
        )
        session.closeWindow() shouldBe value
    }

    test("getWindowHandles") {
        val value = listOf("123", "321")

        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Get, sessionurl(sessionId, "window/handles"),
                buildJsonArray {
                    value.forEach {
                        add(it)
                    }
                }
            ),
        )
        session.getWindowHandles() shouldBe value
    }

    context("switchToFrame") {

        test("switchToFrame Null") {
            val sessionId = UUID.randomUUID().toString()
            val session = mockSession(
                sessionId,
                response(HttpMethod.Post, sessionurl(sessionId, "frame")) { request ->
                    request.asJson().jsonObject["id"] shouldBe JsonNull
                    JsonNull
                },
            )
            session.switchToFrame(SwitchToFrame.Null)
        }

        test("switchToFrame Number") {
            val value = 1
            val sessionId = UUID.randomUUID().toString()
            val session = mockSession(
                sessionId,
                response(HttpMethod.Post, sessionurl(sessionId, "frame")) { request ->
                    request.asJson().jsonObject["id"]?.jsonPrimitive!!.int shouldBe value
                    JsonNull
                },
            )
            session.switchToFrame(SwitchToFrame.Number(value))
        }

        test("switchToFrame WebElement") {
            val elementId = "someElementId"
            val sessionId = UUID.randomUUID().toString()
            val session = mockSession(
                sessionId,
                response(
                    HttpMethod.Get,
                    sessionurl(sessionId, "element/active"),
                    buildJsonObject { put(ELEMENT_KEY, elementId) }),
                response(HttpMethod.Post, sessionurl(sessionId, "frame")) { request ->
                    request.asJson().jsonObject["id"] shouldBe buildJsonObject {
                        put(ELEMENT_KEY, elementId)
                    }
                    JsonNull
                },
            )
            val activeElement = session.getActiveElement()
            session.switchToFrame(SwitchToFrame.WebElement(activeElement))
        }
    }

    test("switchToParentFrame") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "frame/parent")) { request ->
                request.asJson() shouldBe buildJsonObject {
                }
                JsonNull
            },
        )
        session.switchToParentFrame()
    }

    test("getWindowRect") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Get, sessionurl(sessionId, "window/rect"),
                buildJsonObject {
                    put("x", 1)
                    put("y", 1)
                    put("width", 1)
                    put("height", 1)
                }
            ),
        )
        session.getWindowRect() shouldBe Rect(1, 1, 1, 1)
    }
    test("setWindowRect") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "window/rect")) { request ->
                val value = buildJsonObject {
                    put("x", 1)
                    put("y", 1)
                    put("width", 1)
                    put("height", 1)
                }
                request.asJson() shouldBe value
                value
            },
        )
        session.setWindowRect(Rect(1, 1, 1, 1)) shouldBe Rect(1, 1, 1, 1)
    }
    test("windowMaximize") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "window/maximize")) { request ->
                request.asJson() shouldBe buildJsonObject { }
                buildJsonObject {
                    put("x", 1)
                    put("y", 1)
                    put("width", 1)
                    put("height", 1)
                }
            },
        )
        session.windowMaximize() shouldBe Rect(1, 1, 1, 1)
    }
    test("windowMinimize") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "window/minimize")) { request ->
                request.asJson() shouldBe buildJsonObject { }
                buildJsonObject {
                    put("x", 1)
                    put("y", 1)
                    put("width", 1)
                    put("height", 1)
                }
            },
        )
        session.windowMinimize() shouldBe Rect(1, 1, 1, 1)
    }
    test("windowFullscreen") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "window/fullscreen")) { request ->
                request.asJson() shouldBe buildJsonObject { }
                buildJsonObject {
                    put("x", 1)
                    put("y", 1)
                    put("width", 1)
                    put("height", 1)
                }
            },
        )
        session.windowFullscreen() shouldBe Rect(1, 1, 1, 1)
    }

    test("getActiveElement") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Get, sessionurl(sessionId, "element/active"),
                buildJsonObject {
                    put(ELEMENT_KEY, "elementId")
                }
            ),
        )
        session.getActiveElement()
    }

    test("findElement") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "element")) { request ->
                request.asJson() shouldBe
                        buildJsonObject {
                            put("using", "xpath")
                            put("value", "/some")
                        }
                buildJsonObject {
                    put(ELEMENT_KEY, "elementId")
                }
            },
        )
        session.findElement(xpath("/some"))
    }

    test("findElements") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(HttpMethod.Post, sessionurl(sessionId, "elements")) { request ->
                request.asJson() shouldBe
                        buildJsonObject {
                            put("using", "xpath")
                            put("value", "/some")
                        }

                buildJsonArray {
                    add(buildJsonObject {
                        put(ELEMENT_KEY, "elementId")
                    })
                    add(buildJsonObject {
                        put(ELEMENT_KEY, "elementId2")
                    })
                }
            },
        )
        session.findElements(xpath("/some")).size shouldBe 2
    }

    test("getPageSource") {
        val value = "<html></html>"
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Get, sessionurl(sessionId, "source"),
                JsonPrimitive(value)
            ),
        )
        session.getPageSource() shouldBe value
    }

    test("execute") {
        val value = "sometitle"
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Post, sessionurl(sessionId, "execute/sync")
            ) { request ->
                request.asJson() shouldBe buildJsonObject {
                    put("script", "return document.title")
                    put("args", buildJsonArray { })
                }
                JsonPrimitive(value)
            },
        )
        session.execute<String>(ScriptData("return document.title", listOf())) shouldBe value
    }
    test("executeAsync") {
        val value = "sometitle"
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Post, sessionurl(sessionId, "execute/async")
            ) { request ->
                request.asJson() shouldBe buildJsonObject {
                    put("script", "return document.title")
                    put("args", buildJsonArray { })
                }
                JsonPrimitive(value)
            },
        )
        session.executeAsync<String>(ScriptData("return document.title", listOf())) shouldBe value
    }

    test("cookies") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Get, sessionurl(sessionId, "cookie"),
                buildJsonArray {
                    add(buildJsonObject {
                        put("name", "name")
                        put("value", "value")
                    })
                }
            ),
        )
        session.cookies() shouldBe listOf(Cookie("name", "value"))
    }

    test("getCookie") {
        val name = "name"
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Get, sessionurl(sessionId, "cookie/$name"),
                buildJsonObject {
                    put("name", "name")
                    put("value", "value")
                }
            ),
        )
        session.getCookie(name) shouldBe Cookie("name", "value")
    }

    test("setCookie") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Post, sessionurl(sessionId, "cookie")
            ) { request ->
                request.asJson() shouldBe buildJsonObject {
                    put("cookie", buildJsonObject {
                        put("name", "name")
                        put("value", "value")
                    })
                }
                JsonNull
            },
        )
        session.setCookie(CookieData(Cookie("name", "value")))
    }

    test("deleteAllCookie") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Delete, sessionurl(sessionId, "cookie"), JsonNull
            ),
        )
        session.deleteAllCookie()
    }

    test("deleteCookie") {
        val name = "name"
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Delete, sessionurl(sessionId, "cookie/$name"), JsonNull
            ),
        )
        session.deleteCookie(name)
    }

    context("performActions") {
        listOf(
            Tuple3(
                "pause",
                Actions(
                    listOf(
                        Action.NoneAction("pause", listOf(GeneralAction.PauseAction(1000)))
                    )
                ),
                buildJsonObject {
                    put("actions", buildJsonArray {
                        add(buildJsonObject {
                            put("id", "pause")
                            put("type", "none")
                            put("actions", buildJsonArray {
                                add(buildJsonObject {
                                    put("type", "pause")
                                    put("duration", 1000)
                                })
                            })
                        })
                    })
                }),
            Tuple3(
                "keys",
                Actions(
                    listOf(
                        Action.KeyAction(
                            "keyboard", listOf(
                                KeyActionItem.KeyDownAction("w"),
                                KeyActionItem.KeyUpAction("w")
                            )
                        ),
                    )
                ),
                buildJsonObject {
                    put("actions", buildJsonArray {
                        add(buildJsonObject {
                            put("id", "keyboard")
                            put("type", "key")
                            put("actions", buildJsonArray {
                                add(buildJsonObject {
                                    put("type", "keyDown")
                                    put("value", "w")
                                })
                                add(buildJsonObject {
                                    put("type", "keyUp")
                                    put("value", "w")
                                })
                            })
                        })
                    })
                }),
            Tuple3(
                "mouse",
                Actions(
                    listOf(
                        Action.PointerAction(
                            "mouse", listOf(
                                PointerActionItem.Down(0),
                                PointerActionItem.Move(100, Origin.Pointer, 1, 1),
                                PointerActionItem.Move(100, Origin.ViewPort, 1, 1),
                                PointerActionItem.Move(100, Origin.WebElement(WebElementObject("someElementId")), 1, 1),
                                PointerActionItem.Up(0),
                                PointerActionItem.Cancel()
                            )
                        ),
                    )
                ),
                buildJsonObject {
                    put("actions", buildJsonArray {
                        add(buildJsonObject {
                            put("id", "mouse")
                            put("type", "pointer")
                            put("actions", buildJsonArray {
                                add(buildJsonObject {
                                    put("type", "pointerDown")
                                    put("button", 0)
                                })
                                add(buildJsonObject {
                                    put("type", "pointerMove")
                                    put("duration", 100)
                                    put("origin", "pointer")
                                    put("x", 1)
                                    put("y", 1)
                                })
                                add(buildJsonObject {
                                    put("type", "pointerMove")
                                    put("duration", 100)
                                    put("origin", "viewport")
                                    put("x", 1)
                                    put("y", 1)
                                })
                                add(buildJsonObject {
                                    put("type", "pointerMove")
                                    put("duration", 100)
                                    put("origin", buildJsonObject {
                                        put(ELEMENT_KEY, "someElementId")
                                    })
                                    put("x", 1)
                                    put("y", 1)
                                })
                                add(buildJsonObject {
                                    put("type", "pointerUp")
                                    put("button", 0)
                                })
                                add(buildJsonObject {
                                    put("type", "pointerCancel")
                                })
                            })
                        })
                    })
                })
        ).forEach { (name, model, json) ->
            test(name) {
                val sessionId = UUID.randomUUID().toString()
                val session = mockSession(
                    sessionId,
                    response(
                        HttpMethod.Post, sessionurl(sessionId, "actions")
                    ) { request ->
                        request.asJson() shouldBe json
                        JsonNull
                    },
                )
                session.performActions(
                    model
                )
            }
        }

    }

    test("releaseActions") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Delete, sessionurl(sessionId, "actions"), JsonNull
            ),
        )
        session.releaseActions()
    }

    test("dismissAlert") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Post, sessionurl(sessionId, "alert/dismiss"), JsonNull
            ),
        )
        session.dismissAlert()
    }

    test("acceptAlert") {
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Post, sessionurl(sessionId, "alert/accept"), JsonNull
            ),
        )
        session.acceptAlert()
    }

    test("getAlertText") {
        val value = "alert text"
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Get, sessionurl(sessionId, "alert/text"),
                JsonPrimitive(value)
            ),
        )
        session.getAlertText() shouldBe value
    }

    test("sendAlertText") {
        val value = "alert text"
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Post, sessionurl(sessionId, "alert/text")
            ) { request ->
                request.asJson() shouldBe buildJsonObject {
                    put("text", value)
                }
                JsonNull
            }
        )
        session.sendAlertText(Text(value))
    }


    test("takeScreenshot") {
        val value = "somebase64"
        val sessionId = UUID.randomUUID().toString()
        val session = mockSession(
            sessionId,
            response(
                HttpMethod.Get, sessionurl(sessionId, "screenshot"),
                JsonPrimitive(value)
            ),
        )
        session.takeScreenshot() shouldBe value
    }
})

class W3CErrors : FunSpec({
    W3CError.values()
        .forEach { error ->
            test(error.error) {
                val wd = webDriver(
                    {
                        val json = buildJsonObject {
                            put("value", buildJsonObject {
                                put("error", error.error)
                                put("message", "something happens")
                                put("stacktrace", "")
                            })
                        }
                        respond(json.toString(),
                            HttpStatusCode.fromValue(error.httpCode),
                            Headers.build {
                                this["Content-Type"] = "application/json"
                            })
                    }
                )
                shouldThrow<WebDriverException> {
                    wd.status()
                } shouldBe WebDriverException("something happens", error, stacktrace = "")
            }
        }
})


private fun sessionurl(sessionId: String, path: String) = "/session/$sessionId/$path"

private suspend fun mockSession(sessionId: String, vararg handlers: MockRequestHandler): Session {
    val caps = capabilities {
        browserName = "firefox"
    }
    val driver = webDriver(sessionHandler(sessionId), *handlers)
    return driver.session(caps)
}

private fun sessionHandler(sessionId: String): MockRequestHandler {
    val handlers = response(
        HttpMethod.Post,
        "/session"
    ) { request ->
        buildJsonObject {
            put("sessionId", JsonPrimitive(sessionId))
            put(
                "capabilities",
                request.asJson().jsonObject["capabilities"]!!.jsonObject["alwaysMatch"]!!.jsonObject
            )
        }
    }
    return handlers
}

private fun HttpRequestData.asJson() =
    Json.parseToJsonElement((body as TextContent).text)


private fun webDriver(vararg handlers: MockRequestHandler) =
    WebDriver("https://anyUrl") {
        webdriver {
            addErrorConverters(
                listOf(
            w3cConverter()
        )
            )
        }
        json({})
        ktor(MockEngine) {
            engine {
                requestHandlers += handlers
            }
        }
    }

private fun response(
    method: HttpMethod,
    path: String,
    response: JsonElement,
): MockRequestHandler = response(method, path) { response }

private fun response(
    method: HttpMethod,
    path: String,
    response: (HttpRequestData) -> JsonElement,
): MockRequestHandler =
    { request ->
        request.method shouldBe method
        request.url.encodedPath shouldBe path

        val resp = buildJsonObject {
            put(
                "value",
                response(request)
            )
        }
        respond(resp.toString(), headers = Headers.build {
            this["Content-Type"] = "application/json"
        })
    }
