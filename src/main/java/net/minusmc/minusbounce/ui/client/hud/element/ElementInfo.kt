package net.minusmc.minusbounce.ui.client.hud.element


@Retention(AnnotationRetention.RUNTIME)
annotation class ElementInfo(val name: String, val single: Boolean = false, val force: Boolean = false, val disableScale: Boolean = false, val priority: Int = 0, val retrieveDamage: Boolean = false)
