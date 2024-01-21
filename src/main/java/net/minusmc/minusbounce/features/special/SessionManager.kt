package net.minusmc.minusbounce.features.special

import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minecraft.entity.EntityLivingBase

class SessionManager: MinecraftInstance(), Listenable {
	var target: EntityLivingBase? = null

	// Session timer
	var timePlayed = 0L
	val playingTimer = MSTimer()

	// Kills
	var kills = 0
	var gamePlayed = 0

	@EventTarget
	fun onUpdate(event: UpdateEvent) {
		if (!mc.isSingleplayer)
			if (playingTimer.hasTimePassed(500)) {
				timePlayed += 500L
				playingTimer.reset()
			}

		if (target != null && target!!.isDead) {
			kills++
			target = null
		}
	}

	@EventTarget
	fun onAttack(event: AttackEvent) {
		target = event.targetEntity as EntityLivingBase
	}

	override fun handleEvents() = true
}