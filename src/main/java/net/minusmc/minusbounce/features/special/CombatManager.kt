package net.minusmc.minusbounce.features.special

import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.timer.MSTimer

class CombatManager: MinecraftInstance(), Listenable {
	var inCombat = false
	var target: EntityLivingBase? = null
	private val attackedEntityList = mutableListOf<EntityLivingBase>()

	private val attackTimer = MSTimer()

	@EventTarget
	fun onUpdate(event: UpdateEvent) {
		mc.thePlayer ?: return
		attackedEntityList.map {it}.forEach {
			if (it.isDead) {
				MinusBounce.eventManager.callEvent(EntityKilledEvent(it))
				attackedEntityList.remove(it)
			}
		}

		inCombat = false

		if (!attackTimer.hasTimePassed(250)) {
			inCombat = true
			return
		}

		if (target != null) {
			if (mc.thePlayer.getDistanceToEntityBox(target!!) > 7 || !inCombat || target!!.isDead) {
				target = null
			} else {
				inCombat = true
			}
		}
	}

	@EventTarget
	fun onAttack(event: AttackEvent) {
		val target = event.targetEntity

		if (target is EntityLivingBase && EntityUtils.isSelected(target, true)) {
			this.target = target
			if (!attackedEntityList.contains(target)) attackedEntityList.add(target)
		}

		attackTimer.reset()
	}

	@EventTarget
	fun onWorld(event: WorldEvent) {
		attackedEntityList.clear()
		target = null
		inCombat = false
	}

	override fun handleEvents() = true

}