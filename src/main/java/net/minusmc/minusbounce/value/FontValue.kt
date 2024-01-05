/**
 * Font value represents a value with a font
 */

package net.minusmc.minusbounce.value

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.FontUtils
import net.minecraft.client.gui.FontRenderer

open class FontValue(valueName: String, value: FontRenderer, displayable: () -> Boolean) : Value<FontRenderer>(valueName, value, displayable) {

    var openList = false

    constructor(valueName: String, value: FontRenderer): this(valueName, value, { true } )

    override fun toJson(): JsonElement? {
        val fontDetails = Fonts.getFontDetails(value) ?: return null
        val valueObject = JsonObject()
        valueObject.addProperty("fontName", fontDetails[0] as String)
        valueObject.addProperty("fontSize", fontDetails[1] as Int)
        return valueObject
    }

    override fun fromJson(element: JsonElement) {
        if (!element.isJsonObject) return
        val valueObject = element.asJsonObject
        value = Fonts.getFontRenderer(valueObject["fontName"].asString, valueObject["fontSize"].asInt)
    }

    fun changeValue(name: String, size: Int) {
        value = Fonts.getFontRenderer(name, size)
    }

    val values
        get() = FontUtils.getAllFontDetails().map { it.second }

    fun setByName(name: String) {
        set((FontUtils.getAllFontDetails().find { it.first.equals(name, true)} ?: return).second )
    }
}