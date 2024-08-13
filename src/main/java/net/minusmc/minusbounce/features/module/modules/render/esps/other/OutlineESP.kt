package net.minusmc.minusbounce.features.module.modules.render.esps.other

import co.uk.hexeption.utils.OutlineUtils
import net.minecraft.client.renderer.GlStateManager
import net.minusmc.minusbounce.features.module.modules.render.esps.ESPMode
import net.minusmc.minusbounce.event.RenderModelEvent
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.render.GLUtils
import net.minusmc.minusbounce.value.FloatValue

import java.awt.Color

class OutlineESP: ESPMode("Outline") {
	private val outlineWidth = FloatValue("Width", 3f, 0.5f, 5f)

	override fun onRenderModel(event: RenderModelEvent, color: Color) {
		ClientUtils.disableFastRender()
        GlStateManager.resetColor()

        GLUtils.glColor(color)
        OutlineUtils.renderOne(outlineWidth.get())
        event.modelBase.render(event.entity, event.x, event.y, event.z, event.yaw, event.pitch, event.partialTicks)
        GLUtils.glColor(color)
        OutlineUtils.renderTwo()
        event.modelBase.render(event.entity, event.x, event.y, event.z, event.yaw, event.pitch, event.partialTicks)
        GLUtils.glColor(color)
        OutlineUtils.renderThree()
        event.modelBase.render(event.entity, event.x, event.y, event.z, event.yaw, event.pitch, event.partialTicks)
        GLUtils.glColor(color)
        OutlineUtils.renderFour(color)
        event.modelBase.render(event.entity, event.x, event.y, event.z, event.yaw, event.pitch, event.partialTicks)
        GLUtils.glColor(color)
        OutlineUtils.renderFive()
        GLUtils.glColor(Color.WHITE)
	}
}