package net.minusmc.minusbounce.features.module.modules.movement.speeds.matrix

import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType

class Matrix7BalancedSpeed : SpeedMode("Matrix7Balanced", SpeedType.MATRIX) {
    private var ticks = 0
    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        ticks = 0
    }

    override fun onUpdate() {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            ticks++
        }

        if (ticks == 2) {
            mc.timer.timerSpeed = 1.02f
            mc.thePlayer.motionX *= 1.001
            mc.thePlayer.motionZ *= 1.001
        } else if (ticks == 4) {
            mc.timer.timerSpeed = 1f
            ticks = 0
        }
    }
}