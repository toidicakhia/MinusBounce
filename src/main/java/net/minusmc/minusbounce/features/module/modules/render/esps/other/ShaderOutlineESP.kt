package net.minusmc.minusbounce.features.module.modules.render.esps.other

import net.minusmc.minusbounce.event.Render2DEvent
import net.minusmc.minusbounce.features.module.modules.render.esps.ESPMode
import net.minusmc.minusbounce.utils.render.shader.shaders.OutlineShader
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.value.FloatValue
import java.awt.Color

class ShaderOutlineESP: ESPMode("ShaderOutline") {
	private val shaderOutlineRadius = FloatValue("Radius", 1.35f, 1f, 2f, "x")

	override fun onRender2D(event: Render2DEvent, color: Color) {
        OutlineShader.OUTLINE_SHADER.startDraw(event.partialTicks)
        esp.renderNameTags = false

        for (entity in mc.theWorld.loadedEntityList) {
            if (EntityUtils.isSelected(entity, false))
                mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
        }

        esp.renderNameTags = true
        OutlineShader.OUTLINE_SHADER.stopDraw(color, shaderOutlineRadius.get(), 1f)
	}
}