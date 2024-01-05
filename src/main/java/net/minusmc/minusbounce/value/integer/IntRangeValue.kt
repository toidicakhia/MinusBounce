/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.value

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.Gson

open class IntRangeValue(name: String, minValue: Int, maxValue: Int, val minimum: Int = 0, val maximum: Int = Int.MAX_VALUE, val suffix: String = "", displayable: () -> Boolean): Value<IntRange>(name, IntRange(minValue, maxValue), displayable) {
    constructor(name: String, minValue: Int, maxValue: Int, minimum: Int, maximum: Int, displayable: () -> Boolean): this(name, minValue, maxValue, minimum, maximum, "", displayable)
    constructor(name: String, minValue: Int, maxValue: Int, minimum: Int, maximum: Int, suffix: String): this(name, minValue, maxValue, minimum, maximum, suffix, {true})
    constructor(name: String, minValue: Int, maxValue: Int, minimum: Int, maximum: Int): this(name, minValue, maxValue, minimum, maximum, "", {true})

    fun getMinValue() = value.getMin()
    fun getMaxValue() = value.getMax()

    fun setMinValue(newValue: Number) {
        if (newValue.toInt() <= value.getMax()) value.setMin(newValue.toInt())
    }

    fun setMaxValue(newValue: Number) {
        if (newValue.toInt() >= value.getMin()) value.setMax(newValue.toInt())
    }

    fun changeValue(minValue: Int, maxValue: Int) {
        setMaxValue(maxValue)
        setMinValue(minValue)
    }

    fun setForceValue(minValue: Int, maxValue: Int) {
        value.setMax(maxValue)
        value.setMin(minValue)
    }

    override fun toJson(): JsonElement = Gson().toJsonTree(value)
    override fun fromJson(element: JsonElement) {
        if (element.isJsonObject) {
            changeValue(element.asJsonObject["minimum"].asInt, element.asJsonObject["maximum"].asInt)
        }
    }
}