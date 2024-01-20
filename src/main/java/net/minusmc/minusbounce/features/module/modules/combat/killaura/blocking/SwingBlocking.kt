package net.minusmc.minusbounce.features.module.modules.combat.killaura.blocking

import net.minusmc.minusbounce.features.module.modules.combat.killaura.KillAuraBlocking
import net.minusmc.minusbounce.utils.extensions.*

class SwingBlocking: KillAuraBlocking("Swing") {
    override fun onPostMotion() {
    	when (mc.thePlayer.swingProgressInt) {
            1 -> killAura.stopBlocking()
            2 -> killAura.startBlocking()
        }
    }
}