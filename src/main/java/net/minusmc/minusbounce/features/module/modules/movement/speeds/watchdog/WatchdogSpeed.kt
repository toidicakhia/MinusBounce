package net.minusmc.minusbounce.features.module.modules.movement.speeds.watchdog

import net.minecraft.potion.Potion
import net.minecraft.stats.StatList
import net.minusmc.minusbounce.event.JumpEvent
import net.minusmc.minusbounce.event.MotionEvent
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedMode
import net.minusmc.minusbounce.features.module.modules.movement.speeds.SpeedType
import net.minusmc.minusbounce.utils.MovementUtils

class WatchdogSpeed: SpeedMode("Watchdog", SpeedType.WATCHDOG) {

    override fun onJump(event: JumpEvent) {
        if (mc.thePlayer != null && MovementUtils.isMoving)
            event.cancelEvent()
    }

    override fun onMotion(event: MotionEvent) {
        if (MovementUtils.isMoving) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.41999998688698
                mc.thePlayer.isAirBorne = true
                mc.thePlayer.triggerAchievement(StatList.jumpStat)
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MovementUtils.strafe(0.577f)
                } else {
                    MovementUtils.strafe(0.428f)
                }
            }
        }
    }
}