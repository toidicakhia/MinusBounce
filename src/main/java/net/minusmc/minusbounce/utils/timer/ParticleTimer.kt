/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.minusmc.minusbounce.utils.timer

class ParticleTimer {
    var lastMS: Long = 0
    private val time: Long = 0

    private val currentMS: Long
        get() = System.nanoTime() / 1000000L
    private val prevTime: Long = 0
    fun hasReached(milliseconds: Double): Boolean {
        return (this.currentMS - this.lastMS).toDouble() >= milliseconds
    }

    fun setTime(time: Long) {
        lastMS = time
    }

    fun hasTimeElapsed(time: Long): Boolean {
        return System.currentTimeMillis() - lastMS > time
    }

    fun hasPassed(milli: Double): Boolean {
        return System.currentTimeMillis() - this.prevTime >= milli
    }

    fun sleep(time: Long): Boolean {
        if (time() >= time) {
            reset()
            return true
        }
        return false
    }

    fun time(): Long {
        return System.nanoTime() / 1000000L - time
    }

    val elapsedTime: Long
        get() = this.currentMS - this.lastMS

    fun reset() {
        this.lastMS = this.currentMS
    }

    fun delay(milliSec: Float): Boolean {
        return (this.getTime() - this.lastMS).toFloat() >= milliSec
    }

    fun getTime(): Long {
        return System.nanoTime() / 1000000L
    }

    fun isDelayComplete(delay: Long): Boolean {
        return System.currentTimeMillis() - this.lastMS > delay
    }
}