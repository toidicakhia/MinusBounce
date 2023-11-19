/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.movement

import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo

@ModuleInfo(name = "KeepSprint", spacedName = "Keep Sprint", description = "Keep you sprint. Hypixel auto ban.", category = ModuleCategory.MOVEMENT)
class KeepSprint: Module() {
    private var attac = false
    private var motX = 0.0
    private var motZ = 0.0
	
	@EventTarget
    fun onUpdate(event: UpdateEvent) {
    	if(attac) {
    	    mc.thePlayer.motionX = motX
            mc.thePlayer.motionZ = motZ
            mc.thePlayer.isSprinting = true
            attac = false
        }
    }
    
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C0BPacketEntityAction) 
            if (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                attac = true
                motX = mc.thePlayer.motionX
                motZ = mc.thePlayer.motionZ
            }
    }
}
