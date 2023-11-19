package net.minusmc.minusbounce.features.module.modules.combat.criticals.other

import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.features.module.modules.combat.criticals.CriticalMode

class BlocksMCCritical : CriticalMode("BlocksMC") {
    override fun onAttack(event: AttackEvent) {
        val y = mc.thePlayer.posY
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, y + 0.0825080378093, mc.thePlayer.posZ, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, y + 0.0215634532004, mc.thePlayer.posZ, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, y + 0.1040220332227, mc.thePlayer.posZ, false))
    }
}
