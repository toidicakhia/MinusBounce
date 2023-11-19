package net.minusmc.minusbounce.features.module.modules.misc.autoplays

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.features.module.modules.misc.AutoPlay
import net.minusmc.minusbounce.value.Value
import net.minusmc.minusbounce.utils.ClassUtils

abstract class AutoPlayMode(val modeName: String): MinecraftInstance() {

	protected val autoplay: AutoPlay
		get() = MinusBounce.moduleManager[AutoPlay::class.java]!!

	open fun onUpdate() {}
	open fun onPacket() {}

}