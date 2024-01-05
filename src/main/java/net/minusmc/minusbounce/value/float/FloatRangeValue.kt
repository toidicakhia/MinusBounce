/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.value

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.Gson

open class FloatRangeValue(name: String, minValue: Float, maxValue: Float, val minimum: Float = 0f, val maximum: Float = Float.MAX_VALUE, val suffix: String = "", displayable: () -> Boolean): Value<FloatRange>(name, FloatRange(minValue, maxValue), displayable) {
    constructor(name: String, minValue: Float, maxValue: Float, minimum: Float, maximum: Float, displayable: () -> Boolean): this(name, minValue, maxValue, minimum, maximum, "", displayable)
    constructor(name: String, minValue: Float, maxValue: Float, minimum: Float, maximum: Float, suffix: String): this(name, minValue, maxValue, minimum, maximum, suffix, {true})
    constructor(name: String, minValue: Float, maxValue: Float, minimum: Float, maximum: Float): this(name, minValue, maxValue, minimum, maximum, "", {true})

    fun getMinValue() = value.getMin()
    fun getMaxValue() = value.getMax()

    fun setMinValue(newValue: Number) {
        if (newValue.toFloat() <= value.getMax()) value.setMin(newValue.toFloat())
    }

    fun setMaxValue(newValue: Number) {
        if (newValue.toFloat() >= value.getMin()) value.setMax(newValue.toFloat())
    }

    fun changeValue(minValue: Float, maxValue: Float) {
        setMaxValue(maxValue)
        setMinValue(minValue)
    }

    fun setForceValue(minValue: Float, maxValue: Float) {
        if (minValue > maxValue) {
            value.setMax(minValue)
            value.setMin(maxValue)
        } else {
            value.setMax(maxValue)
            value.setMin(minValue)
        }
        
    }

    override fun toJson(): JsonElement = Gson().toJsonTree(value)
    override fun fromJson(element: JsonElement) {
        if (element.isJsonObject) {
            setForceValue(element.asJsonObject["minimum"].asFloat, element.asJsonObject["maximum"].asFloat)
        }
    }
}