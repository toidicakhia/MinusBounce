package net.minusmc.minusbounce.features.module.modules.render.esps.normal

import net.minecraft.entity.EntityLivingBase
import net.minusmc.minusbounce.features.module.modules.render.esps.ESPMode
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.GLUtils
import net.minusmc.minusbounce.utils.misc.MathUtils

import org.lwjgl.opengl.GL11
import java.awt.Color

class Real2DESP: ESPMode("Real2D") {
    private val real2dcsgo = BoolValue("CSGOStyle", true)
    private val real2dShowHealth = BoolValue("ShowHealth", true)
    private val real2dShowHeldItem = BoolValue("ShowHeldItem", true)
    private val real2dShowName = BoolValue("ShowEntityName", true)
    private val real2dOutline = BoolValue("Outline", true)
    
	override fun onEntityRender(entity: EntityLivingBase, color: Color) {
        val posX = MathUtils.interpolate(entity.posX, entity.lastTickPosX, mc.timer.renderPartialTicks)
        val posY = MathUtils.interpolate(entity.posY, entity.lastTickPosY, mc.timer.renderPartialTicks)
        val posZ = MathUtils.interpolate(entity.posZ, entity.lastTickPosZ, mc.timer.renderPartialTicks)

        val bb = entity.entityBoundingBox
            .offset(-entity.posX, -entity.posY, -entity.posZ)
            .offset(posX, posY, posZ)
            .offset(-renderManager.renderPosX, -renderManager.renderPosY, -renderManager.renderPosZ)
        
        val boxVertices = arrayOf(
            Vector3f(bb.minX, bb.minY, bb.minZ),
            Vector3f(bb.minX, bb.maxY, bb.minZ),
            Vector3f(bb.maxX, bb.maxY, bb.minZ),
            Vector3f(bb.maxX, bb.minY, bb.minZ),
            Vector3f(bb.minX, bb.minY, bb.maxZ),
            Vector3f(bb.minX, bb.maxY, bb.maxZ),
            Vector3f(bb.maxX, bb.maxY, bb.maxZ),
            Vector3f(bb.maxX, bb.minY, bb.maxZ)
        )

        var minX = mc.displayWidth.toFloat()
        var minY = mc.displayHeight.toFloat()
        var maxX = 0f
        var maxY = 0f
        for (boxVertex in boxVertices) {
            val screenPos = worldToScreen(boxVertex, mvMatrix, projectionMatrix, mc.displayWidth, mc.displayHeight) ?: continue
            
            minX = min(screenPos.x, minX)
            minY = min(screenPos.y, minY)
            maxX = max(screenPos.x, maxX)
            maxY = max(screenPos.y, maxY)
        }

        if (minX >= mc.displayWidth || minY >= mc.displayHeight || maxX <= 0 || maxY <= 0)
            return

        if (real2dOutline.get()) {
            GL11.glLineWidth(2f)
            GL11.glColor4f(0f, 0f, 0f, 1.0f)
            if (real2dcsgo.get()) {
                val distX = (maxX - minX) / 3f
                val distY = (maxY - minY) / 3f
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex2f(minX, minY + distY)
                GL11.glVertex2f(minX, minY)
                GL11.glVertex2f(minX + distX, minY)
                GL11.glEnd()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex2f(minX, maxY - distY)
                GL11.glVertex2f(minX, maxY)
                GL11.glVertex2f(minX + distX, maxY)
                GL11.glEnd()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex2f(maxX - distX, minY)
                GL11.glVertex2f(maxX, minY)
                GL11.glVertex2f(maxX, minY + distY)
                GL11.glEnd()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex2f(maxX - distX, maxY)
                GL11.glVertex2f(maxX, maxY)
                GL11.glVertex2f(maxX, maxY - distY)
                GL11.glEnd()
            } else {
                GL11.glBegin(GL11.GL_LINE_LOOP)
                GL11.glVertex2f(minX, minY)
                GL11.glVertex2f(minX, maxY)
                GL11.glVertex2f(maxX, maxY)
                GL11.glVertex2f(maxX, minY)
                GL11.glEnd()
            }
            GL11.glLineWidth(1.0f)
        }

        GLUtils.glColor(color)

        if (real2dcsgo.get()) {
            val distX = (maxX - minX) / 3f
            val distY = (maxY - minY) / 3f
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex2f(minX, minY + distY)
            GL11.glVertex2f(minX, minY)
            GL11.glVertex2f(minX + distX, minY)
            GL11.glEnd()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex2f(minX, maxY - distY)
            GL11.glVertex2f(minX, maxY)
            GL11.glVertex2f(minX + distX, maxY)
            GL11.glEnd()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex2f(maxX - distX, minY)
            GL11.glVertex2f(maxX, minY)
            GL11.glVertex2f(maxX, minY + distY)
            GL11.glEnd()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            GL11.glVertex2f(maxX - distX, maxY)
            GL11.glVertex2f(maxX, maxY)
            GL11.glVertex2f(maxX, maxY - distY)
            GL11.glEnd()
        } else {
            GL11.glBegin(GL11.GL_LINE_LOOP)
            GL11.glVertex2f(minX, minY)
            GL11.glVertex2f(minX, maxY)
            GL11.glVertex2f(maxX, maxY)
            GL11.glVertex2f(maxX, minY)
            GL11.glEnd()
        }

        if (real2dShowHealth.get()) {
            val barHeight = (maxY - minY) * (1 - entity.health / entity.maxHealth)
            GL11.glColor4f(0.1f, 1f, 0.1f, 1f)
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glVertex2f(maxX + 2, minY + barHeight)
            GL11.glVertex2f(maxX + 2, maxY)
            GL11.glVertex2f(maxX + 4, maxY)
            GL11.glVertex2f(maxX + 4, minY + barHeight)
            GL11.glEnd()
            GL11.glColor4f(1f, 1f, 1f, 1f)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)

            val hp = decimalFormat.format(entity.health.toDouble()) + " HP"

            mc.fontRendererObj.drawStringWithShadow(hp, maxX + 4, minY + barHeight, -1)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GlStateManager.resetColor()
        }

        if (real2dShowHeldItem.get() && entity.heldItem?.item != null) {
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            val stringWidth = mc.fontRendererObj.getStringWidth(entity.heldItem.displayName)
            mc.fontRendererObj.drawStringWithShadow(entity.heldItem.displayName, minX + (maxX - minX) / 2 - stringWidth / 2, maxY + 2, -1)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
        }

        if (real2dShowName.get()) {
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            val stringWidth = mc.fontRendererObj.getStringWidth(entity.displayName.formattedText)
            mc.fontRendererObj.drawStringWithShadow(entity.displayName.formattedText, minX + (maxX - minX) / 2 - stringWidth / 2, minY - 12, -1)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
        }
    }
}