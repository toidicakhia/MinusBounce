/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.value

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.Gson
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.extensions.setAlpha
import net.minecraft.client.gui.FontRenderer
import net.minusmc.minusbounce.utils.FontUtils
import java.awt.Color
import java.util.*

abstract class Value<T>(var name: String, protected var value: T, var canDisplay: () -> Boolean) {
    val defaultValue = value
    val displayableFunction: () -> Boolean
        get() = canDisplay

    fun displayable(func: () -> Boolean): Value<T> {
        canDisplay = func
        return this
    }

    fun get() = value

    fun set(newValue: T) {
        if (newValue == value) return

        val oldValue = get()

        try {
            onChange(oldValue, newValue)
            changeValue(newValue)
            onChanged(oldValue, newValue)
        } catch (e: Exception) {
            ClientUtils.logger.error("[ValueSystem ($name)]: ${e.javaClass.name} (${e.message}) [$oldValue >> $newValue]")
        }
    }

    open fun changeValue(value: T) {
        this.value = value
    }

    abstract fun toJson(): JsonElement?
    abstract fun fromJson(element: JsonElement)

    protected open fun onChange(oldValue: T, newValue: T) {}
    protected open fun onChanged(oldValue: T, newValue: T) {}

}