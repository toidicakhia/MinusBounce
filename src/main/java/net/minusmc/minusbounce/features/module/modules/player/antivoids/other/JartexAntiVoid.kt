package net.minusmc.minusbounce.features.module.modules.player.antivoids.other

import net.minusmc.minusbounce.features.module.modules.player.antivoids.AntiVoidMode
import net.minusmc.minusbounce.event.*
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook


class JartexAntiVoid: AntiVoidMode("Jartex") {
    private var canSpoof = false
    private var flagged = false
    private var lastRecY = 0.0

    override fun onEnable() {
        canSpoof = false
        flagged = false
    }

    override fun onWorld() {
        if (lastRecY == 0.0)
            lastRecY = mc.thePlayer.posY
    }

    override fun onUpdate() {
        canSpoof = false
    }

    override fun onUpdateVoided() {
        if (mc.thePlayer.fallDistance > antivoid.maxFallDistValue.get() && mc.thePlayer.posY < lastRecY + 0.01 && mc.thePlayer.motionY <= 0 && !mc.thePlayer.onGround && !flagged) {
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionZ *= 0.838
            mc.thePlayer.motionX *= 0.838
            canSpoof = true
        }
    }

    override fun onPostVoided() {
        lastRecY = mc.thePlayer.posY
    }

    override fun onSentPacket(event: SentPacketEvent) {
        val packet = event.packet

        if (canSpoof && packet is C03PacketPlayer)
            packet.onGround = true
    }

    override fun onReceivedPacket(event: ReceivedPacketEvent) {
        val packet = event.packet

        if (canSpoof && packet is S08PacketPlayerPosLook)
            flagged = true
    }
}