package net.minusmc.minusbounce.features.module.modules.movement.noslows.matrix

import net.minusmc.minusbounce.features.module.modules.movement.noslows.NoSlowMode

class MatrixSlowNoSlow : NoSlowMode("MatrixSlow") {
    override fun onUpdate() {
        if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown()) {
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                mc.thePlayer.motionX *= 0.46
                mc.thePlayer.motionZ *= 0.46
            }
        } else if (mc.thePlayer.fallDistance > 0.2) {
            mc.thePlayer.motionX *= 0.9100000262260437
            mc.thePlayer.motionZ *= 0.9100000262260437
        }
    }
}