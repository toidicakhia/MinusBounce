package net.minusmc.minusbounce.features.module.modules.render.esps.other

import net.minusmc.minusbounce.features.module.modules.render.esps.ESPMode
import net.minusmc.minusbounce.event.RenderModelEvent
import net.minusmc.minusbounce.utils.render.GLUtils
import net.minusmc.minusbounce.value.FloatValue
import org.lwjgl.opengl.GL11
import java.awt.Color

class WireframeESP: ESPMode("Wireframe") {
	private val wireframeWidth = FloatValue("Width", 2f, 0.5f, 5f)
    
	override fun onRenderModel(event: RenderModelEvent, color: Color) {
		GL11.glPushMatrix()
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GLUtils.glColor(color)
        GL11.glLineWidth(wireframeWidth.get())
        event.modelBase.render(event.entity, event.x, event.y, event.z, event.yaw, event.pitch, event.partialTicks)
        GL11.glPopAttrib()
        GL11.glPopMatrix()
	}
}