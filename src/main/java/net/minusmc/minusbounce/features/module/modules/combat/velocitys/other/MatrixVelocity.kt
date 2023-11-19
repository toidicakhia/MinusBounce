package net.minusmc.minusbounce.features.module.modules.combat.velocitys.other

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode

class MatrixVelocity : VelocityMode("Matrix") {
    override fun onUpdate() {
        if (mc.thePlayer.hurtTime <= 0) {
            return
        }
        if (mc.thePlayer.onGround) {
            if (mc.thePlayer.hurtTime <= 6) {
                mc.thePlayer.motionX *= 0.700054132
                mc.thePlayer.motionZ *= 0.700054132
            }
            if (mc.thePlayer.hurtTime <= 5) {
                mc.thePlayer.motionX *= 0.803150645
                mc.thePlayer.motionZ *= 0.803150645
            }
        } else if (mc.thePlayer.hurtTime <= 10) {
            mc.thePlayer.motionX *= 0.605001
            mc.thePlayer.motionZ *= 0.605001
        }
    }
}