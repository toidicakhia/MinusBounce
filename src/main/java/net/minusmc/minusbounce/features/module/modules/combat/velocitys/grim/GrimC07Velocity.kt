package net.minusmc.minusbounce.features.module.modules.combat.velocitys.grim

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.ui.client.hud.element.elements.Notification
import net.minusmc.minusbounce.utils.PacketUtils
import net.minusmc.minusbounce.value.ListValue
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.util.EnumFacing

class GrimC07Velocity : VelocityMode("GrimC07") {
    private var onVelocity = ListValue("OnVelocity", arrayOf("Always", "CombatManager", "PacketDamage"))
    private var packetPayloadValue = ListValue("PacketPayload", arrayOf("C03", "C06"), "C03")
    private var canSpoof = false
    private var canCancel = false

    override fun onEnable() {
        canSpoof = false
        canCancel = false
    }

    override fun onUpdate() {
        if (onVelocity.equals("always") || (onVelocity.equals("combatmanager") && MinusBounce.combatManager.inCombat))
            canCancel = true

        if (canSpoof) {
            val pos = mc.thePlayer.getPosition()

            when (packetPayloadValue.get().lowercase()) {
                "c03" -> PacketUtils.sendPacketNoEvent(C03PacketPlayer(mc.thePlayer.onGround))
                "c06" -> PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround))
            }
            PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.DOWN))
            canSpoof = false
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S19PacketEntityStatus && onVelocity.get().equals("PacketDamage", true)) {
            val player = packet.getEntity(mc.theWorld)
            if (player != mc.thePlayer || packet.opCode != 2.toByte()) 
                return
            canCancel = true
        }

        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId && canCancel) {
            event.cancelEvent()
            canCancel = false
            canSpoof = true
        }
    }
}