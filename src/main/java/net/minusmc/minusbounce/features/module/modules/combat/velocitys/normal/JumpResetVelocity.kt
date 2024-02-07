package net.minusmc.minusbounce.features.module.modules.combat.velocitys.normal

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.value.BoolValue

class JumpResetVelocity: VelocityMode("JumpReset") {
    private val onMouse = BoolValue("OnMouseDown", false)
    
    override fun onUpdate() {
        if (mc.thePlayer.hurtTime > 8 && mc.thePlayer.onGround && MinusBounce.combatManager.inCombat && (!onMouse.get() || mc.gameSettings.keyBindAttack.isKeyDown))
            mc.thePlayer.jump()
    }
}
