package net.minusmc.minusbounce.features.special

import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.timer.MSTimer

class CombatManager: MinecraftInstance(), Listenable {
	val discoveredEntities = mutableListOf<EntityLivingBase>()
	private val prevDiscoveredEntites = mutableListOf<Int>()
	
	var target: EntityLivingBase? = null

	@EventTarget
	fun onUpdate(event: UpdateEvent) {
		mc.theWorld ?: return

		discoveredEntities.clear()

		for (entity in mc.theWorld.loadedEntityList) {
			if (entity !is EntityLivingBase || !EntityUtils.isEnemy(entity) || (switchMode && prevDiscoveredEntites.contains(entity.entityId)))
                continue

			if (!discoveredEntities.contains(entity) && mc.thePlayer.getDistanceToEntityBox(entity) <= 15)
				discoveredEntities.add(entity)
		}

		if (discoveredEntities.isEmpty()) {
			prevDiscoveredEntites.clear()
			return
		}

		if (target != null && target!!.isDead) {
			discoveredEntities.remove(target)
			target = null
		}
	}

	@EventTarget
	fun onWorld(event: WorldEvent) {
		discoveredEntities.clear()
		prevDiscoveredEntites.clear()
	}
	
	fun nextEntity() {
		val entity = target ?: return
		discoveredEntities.remove(entity)
		prevDiscoveredEntites.add(entity.entityId)
	}

	fun sortEntities(priority: String) {
		when (priority.lowercase()) {
			"distance" -> discoveredEntities.sortBy { mc.thePlayer.getDistanceToEntityBox(it) }
			"health" -> discoveredEntities.sortBy { it.health + it.absorptionAmount }
			"hurtresistance" -> discoveredEntities.sortBy { it.hurtResistantTime }
			"hurttime" -> discoveredEntities.sortBy { it.hurtTime }
			"armor" -> discoveredEntities.sortBy { it.totalArmorValue }
		}
	}

	fun getEntitiesInRange(range: Float, limit: Int = Int.MAX_VALUE) = discoveredEntities.filter {mc.thePlayer.getDistanceToEntityBox(it) <= range}.take(limit)

	val inCombat: Boolean
		get() = target != null

	val switchMode: Boolean
		get() = MinusBounce.moduleManager[KillAura::class.java]!!.targetModeValue.get().equals("Switch", true)

	override fun handleEvents() = true
}