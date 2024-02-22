/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.special

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.utils.misc.MathUtils

import kotlin.math.*

object MovementCorrection: MinecraftInstance(), Listenable {
    @JvmField
	var fixType = Type.NONE

    @EventTarget(priority = 2)
    fun onMoveInput(event: MoveInputEvent) {
        if (fixType != Type.STRICT) return

        val rotation = RotationUtils.targetRotation
        println(rotation)

        if (rotation != null) {
            val forward = event.forward.toFloat()
            val strafe = event.strafe.toFloat()

            val offset = MathUtils.toRadians(mc.thePlayer.rotationYaw - rotation.yaw)
            val cosValue = cos(offset)
            val sinValue = sin(offset)

            println("sin = $sinValue cos = $cosValue")

            event.forward = round(forward * cosValue + strafe * sinValue)
            event.strafe = round(strafe * cosValue - forward * sinValue)
        }
    }

    @EventTarget(priority = 2)
    fun onJump(event: JumpEvent) {
        if (fixType != Type.NONE) {
        	val rotation = RotationUtils.targetRotation ?: return
        	event.yaw = rotation.yaw
        }
    }

    @EventTarget(priority = 2)
    fun onStrafe(event: StrafeEvent) {
        if (fixType != Type.NONE) {
        	val rotation = RotationUtils.targetRotation ?: return
        	event.yaw = rotation.yaw
        }
    }

    override fun handleEvents() = true

    enum class Type {
        STRICT, NORMAL, NONE
    }
}