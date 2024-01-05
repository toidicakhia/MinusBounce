/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.value

open abstract class MinMaxRange<T>(protected var minimum: T, protected var maximum: T) {
    fun getMin() = minimum
    fun getMax() = maximum
    fun setMin(value: T) {
        this.minimum = value
    }
    fun setMax(value: T) {
        this.maximum = value
    }
}