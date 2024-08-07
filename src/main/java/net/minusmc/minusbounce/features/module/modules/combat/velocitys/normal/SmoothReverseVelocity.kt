package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.event.ReceivedPacketEvent
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.utils.timer.MSTimer

class SmoothReverseVelocity : VelocityMode("SmoothReverse") {
    private val reverse2StrengthValue = FloatValue("Strength", 0.05f, 0.02f, 0.1f, "x")
    
    private var velocityInput = false
    private var reverseHurt = false
    private val velocityTimer = MSTimer()

    override fun onUpdate() {
        if (!velocityInput) {
            mc.thePlayer.speedInAir = 0.02F
            return
        }

        if (mc.thePlayer.hurtTime > 0)
            reverseHurt = true

        if (!mc.thePlayer.onGround) {
            if (reverseHurt)
                mc.thePlayer.speedInAir = reverse2StrengthValue.get()
        } else if (velocityTimer.hasTimePassed(80)) {
            velocityInput = false
            reverseHurt = false
        }
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        if (event.packet is S12PacketEntityVelocity)
            velocityInput = true
    }
}