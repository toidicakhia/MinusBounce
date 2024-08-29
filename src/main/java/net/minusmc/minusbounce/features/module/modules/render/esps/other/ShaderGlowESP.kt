package net.minusmc.minusbounce.features.module.modules.render.esps.other

import net.minecraft.entity.Entity
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.modules.render.esps.ESPMode
import net.minusmc.minusbounce.utils.render.shader.shaders.GlowShader
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.value.FloatValue
import java.awt.Color

class ShaderGlowESP: ESPMode("ShaderGlow") {
	private val shaderGlowRadius = FloatValue("Radius", 2.3f, 2f, 3f, "x")
    private val entities = mutableListOf<Entity>()

	override fun onRender2D(event: Render2DEvent, color: Color) {
        entities.clear()
        GlowShader.startDraw(event.partialTicks)

        for (entity in mc.theWorld.loadedEntityList) {
            if (EntityUtils.isSelected(entity, true)) {
                entities.add(entity)
                mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
            }
        }

        GlowShader.stopDraw(color, shaderGlowRadius.get(), 1f)
	}

    override fun onRenderNameTags(event: RenderNameTagsEvent) {
        if (event.entity in entities)
            event.isCancelled = true
    }
}