package net.minusmc.minusbounce.features.module.modules.render.esps.other

import net.minecraft.entity.Entity
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.modules.render.esps.ESPMode
import net.minusmc.minusbounce.utils.render.shader.shaders.OutlineShader
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.value.FloatValue
import java.awt.Color

class ShaderOutlineESP: ESPMode("ShaderOutline") {
	private val shaderOutlineRadius = FloatValue("Radius", 1.35f, 1f, 2f, "x")

    private val entities = mutableListOf<Entity>()

	override fun onRender2D(event: Render2DEvent, color: Color) {
        entities.clear()
        OutlineShader.startDraw(event.partialTicks)

        for (entity in mc.theWorld.loadedEntityList) {
            if (EntityUtils.isSelected(entity, true)) {
                entities.add(entity)
                mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
            }
        }

        OutlineShader.stopDraw(color, shaderOutlineRadius.get(), 1f)
	}

    override fun onRenderNameTags(event: RenderNameTagsEvent) {
        if (event.entity in entities)
            event.isCancelled = true
    }
}