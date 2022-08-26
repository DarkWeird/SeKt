package me.darkweird.sekt.w3c

import me.darkweird.sekt.core.*

suspend inline fun <reified R> WebElement.get(path: String): R =
    try {
        session.get("/element/$elementId$path")
    } catch (e: WebDriverException) {
        if (e.kind == W3CError.STALE_ELEMENT_REFERENCE) {
            refreshFn()
            session.get("/element/$elementId$path")
        } else {
            throw e
        }
    }


suspend inline fun <reified R> WebElement.delete(path: String): R =
    try {
        session.delete("/element/$elementId$path")
    } catch (e: WebDriverException) {
        if (e.kind == W3CError.STALE_ELEMENT_REFERENCE) {
            refreshFn()
            session.delete("/element/$elementId$path")
        } else {
            throw e
        }
    }


suspend inline fun <reified T : Any, reified R> WebElement.post(path: String, body: T): R =
    try {
        session.post("/element/$elementId$path", body)
    } catch (e: WebDriverException) {
        if (e.kind == W3CError.STALE_ELEMENT_REFERENCE) {
            refreshFn()
            session.post("/element/$elementId$path", body)
        } else {
            throw e
        }
    }



