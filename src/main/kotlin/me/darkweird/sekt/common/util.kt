package me.darkweird.sekt.common

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive


public fun JsonElement.asObj(): JsonObject? = (this as? JsonObject)

public fun JsonElement?.getString(): String = (this as? JsonPrimitive)?.content ?: ""