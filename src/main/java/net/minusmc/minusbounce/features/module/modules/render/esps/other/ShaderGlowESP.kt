package net.minusmc.minusbounce.features.module.modules.render.esps.other

import net.minusmc.minusbounce.event.Render2DEvent
import net.minusmc.minusbounce.features.module.modules.render.esps.ESPMode
import net.minusmc.minusbounce.utils.render.shader.shaders.GlowShader
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.value.FloatValue
import java.awt.Color

class ShaderGlowESP: ESPMode("ShaderGlow") {
	private val shaderGlowRadius = FloatValue("Radius", 2.3f, 2f, 3f, "x")
    
	override fun onRender2D(event: Render2DEvent, color: Color) {
        GlowShader.GLOW_SHADER.startDraw(event.partialTicks)
        esp.renderNameTags = false

        for (entity in mc.theWorld.loadedEntityList) {
            if (EntityUtils.isSelected(entity, false))
                mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
        }

        esp.renderNameTags = true
        GlowShader.GLOW_SHADER.stopDraw(color, shaderGlowRadius.get(), 1f)
	}
}