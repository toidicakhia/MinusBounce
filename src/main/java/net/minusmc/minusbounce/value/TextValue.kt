package net.minusmc.minusbounce.value

import com.google.gson.JsonPrimitive
import com.google.gson.JsonElement


open class TextValue(name: String, value: String, displayable: () -> Boolean) : Value<String>(name, value, displayable) {

    constructor(name: String, value: String): this(name, value, { true } )

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive)
            value = element.asString
    }
}