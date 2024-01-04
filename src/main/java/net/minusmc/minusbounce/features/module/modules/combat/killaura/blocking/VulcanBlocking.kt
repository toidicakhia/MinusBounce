package net.minusmc.minusbounce.features.module.modules.combat.killaura.blocking

import net.minusmc.minusbounce.features.module.modules.combat.killaura.KillAuraBlocking
import net.minusmc.minusbounce.utils.timer.MSTimer

class VulcanBlocking: KillAuraBlocking("Vulcan") {
    private val blockTimer = MSTimer()

    override fun onPreAttack(){
        killAura.stopBlocking()
    }

    override fun onPostAttack(){
        if (blockTimer.hasTimePassed(50)) {
            PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
            blockTimer.reset()
        }
    }
}