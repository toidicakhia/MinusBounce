package net.minusmc.minusbounce.features.module.modules.combat

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.EntityUtils
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer

// From LB+r
@ModuleInfo(name = "TimerRange", description = "Automatically speeds up/down when you are near an enemy.", category = ModuleCategory.COMBAT)
class TimerRange : Module() {
    private val rangeStart: FloatValue = object : FloatValue("Range-Start", 3.5F, 0.0F, 6.0F) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (rangeStop.get() > newValue) {
                set(rangeStop.get())
            }
        }
    }
    private val rangeStop: FloatValue = object : FloatValue("Range-Stop", 3.4F, 0.0F, 6.0F) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (rangeStart.get() < newValue) {
                set(rangeStart.get())
            }
        }
    }
    private val timerValue = FloatValue("Timer", 1.5F, 0.1F, 2F)
    
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityPlayer && EntityUtils.isSelected(entity, true)) {
                if (mc.thePlayer.getDistanceToEntity(entity) <= rangeStart.get()) {
                    mc.timer.timerSpeed = timerValue.get()
                }
                if (mc.thePlayer.getDistanceToEntity(entity) <= rangeStop.get()) {
                    mc.timer.timerSpeed = 1.0F
                }
                if (mc.thePlayer.getDistanceToEntity(entity) > rangeStart.get()) {
                    mc.timer.timerSpeed = 1.0F
                }
            }
        }
    }

    override fun onEnable() {
        mc.timer.timerSpeed = 1.0F
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0F
    }
}
