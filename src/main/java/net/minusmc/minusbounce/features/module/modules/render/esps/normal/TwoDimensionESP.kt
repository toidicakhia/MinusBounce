package net.minusmc.minusbounce.features.module.modules.render.esps.normal

import net.minecraft.entity.EntityLivingBase
import net.minusmc.minusbounce.features.module.modules.render.esps.ESPMode
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.misc.MathUtils

import java.awt.Color

class TwoDimensionESP: ESPMode("2D") {
	override fun onEntityRender(entity: EntityLivingBase, color: Color) {
		val posX = MathUtils.interpolate(entity.posX, entity.lastTickPosX, mc.timer.renderPartialTicks) - mc.renderManager.renderPosX
        val posY = MathUtils.interpolate(entity.posY, entity.lastTickPosY, mc.timer.renderPartialTicks) - mc.renderManager.renderPosY
        val posZ = MathUtils.interpolate(entity.posZ, entity.lastTickPosZ, mc.timer.renderPartialTicks) - mc.renderManager.renderPosZ
        
        RenderUtils.draw2D(entity, posX, posY, posZ, color.rgb, Color.BLACK.rgb)
	}
}