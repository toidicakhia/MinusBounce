package net.minusmc.minusbounce.features.module.modules.movement

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo

import kotlin.math.sin
import kotlin.math.cos

@ModuleInfo(name = "FloMode", description = "Move as Florentino.", category = ModuleCategory.MOVEMENT)
class FloMode : Module() {
    private var step = 0

    @EventTarget
    fun onUpdate(event: UpdateEvent) {

        val sinyaw = sin(Math.toRadians(mc.thePlayer.rotationYaw.toDouble()))
        val cosyaw = -cos(Math.toRadians(mc.thePlayer.rotationYaw.toDouble()))

        when (step) {
            0 -> mc.thePlayer.setPosition(mc.thePlayer.posX + sinyaw * 1, mc.thePlayer.posY, mc.thePlayer.posZ + cosyaw * 0.5)
            1 -> mc.thePlayer.setPosition(mc.thePlayer.posX - cosyaw * 1, mc.thePlayer.posY, mc.thePlayer.posZ - sinyaw * 0.5)
            2 -> mc.thePlayer.setPosition(mc.thePlayer.posX + cosyaw * 1, mc.thePlayer.posY, mc.thePlayer.posZ + sinyaw * 0.5)
            3 -> mc.thePlayer.setPosition(mc.thePlayer.posX - cosyaw * 1, mc.thePlayer.posY, mc.thePlayer.posZ - sinyaw * 0.5)
        }

        step++
        if (step > 3) {
            step = 0 
        }
    }

    override fun onDisable() {
        mc.gameSettings.keyBindForward.pressed = false
    }
}
