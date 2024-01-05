package net.minusmc.minusbounce.features.module.modules.combat.killaura.blocking

import net.minusmc.minusbounce.features.module.modules.combat.killaura.KillAuraBlocking
import net.minusmc.minusbounce.utils.extensions.*

class SwingBlocking: KillAuraBlocking("Swing") {
    override fun onPostMotion() {
    	when (mc.thePlayer.swingProgressInt) {
            1 -> killAura.stopBlocking()
            2 -> {
                if (killAura.target != null && mc.thePlayer.getDistanceToEntityBox(killAura.target!!) < killAura.rangeValue.get())
                    killAura.startBlocking()
            }
        }
    }
}