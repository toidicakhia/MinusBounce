package net.minusmc.minusbounce.features.module.modules.render.esps.normal

import net.minecraft.entity.EntityLivingBase
import net.minusmc.minusbounce.features.module.modules.render.esps.ESPMode
import net.minusmc.minusbounce.utils.render.RenderUtils

import java.awt.Color

class BoxESP: ESPMode("Box") {
	override fun onEntityRender(entity: EntityLivingBase, color: Color) {
		RenderUtils.drawEntityBox(entity, color, false)
	}
}