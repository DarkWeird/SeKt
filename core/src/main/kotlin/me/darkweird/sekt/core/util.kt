package me.darkweird.sekt.core

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


fun JsonElement.asObj(): JsonObject? = (this as? JsonObject)

fun JsonElement?.getString(): String = (this as? JsonPrimitive)?.content ?: ""