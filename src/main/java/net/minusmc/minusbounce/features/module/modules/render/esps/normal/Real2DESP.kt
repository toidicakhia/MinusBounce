package net.minusmc.minusbounce.features.module.modules.render.esps.normal

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minusmc.minusbounce.event.RenderNameTagsEvent
import net.minusmc.minusbounce.features.module.modules.render.esps.ESPMode
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.GLUtils
import net.minusmc.minusbounce.utils.render.WorldToScreen
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.value.BoolValue

import org.lwjgl.util.vector.Vector3f
import kotlin.math.*
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat

class Real2DESP: ESPMode("Real2D") {
    private val real2dcsgo = BoolValue("CSGOStyle", true)
    private val real2dShowHealth = BoolValue("ShowHealth", true)
    private val real2dShowHeldItem = BoolValue("ShowHeldItem", true)
    private val real2dShowName = BoolValue("ShowEntityName", true)
    private val real2dOutline = BoolValue("Outline", true)
    
    private val decimalFormat = DecimalFormat("0.0")
    private var mvMatrix = WorldToScreen.getMatrix(GL11.GL_MODELVIEW_MATRIX)
    private var projectionMatrix = WorldToScreen.getMatrix(GL11.GL_PROJECTION_MATRIX)

    private val entities = mutableListOf<EntityLivingBase>()

    override fun onPreRender3D() {
        mvMatrix = WorldToScreen.getMatrix(GL11.GL_MODELVIEW_MATRIX)
        projectionMatrix = WorldToScreen.getMatrix(GL11.GL_PROJECTION_MATRIX)

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glOrtho(0.0, mc.displayWidth.toDouble(), mc.displayHeight.toDouble(), 0.0, -1.0, 1.0)
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glPushMatrix()
        GL11.glLoadIdentity()
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableTexture2D()
        GlStateManager.depthMask(true)
        GL11.glLineWidth(1.0f)

        entities.clear()
    }

    override fun onPostRender3D() {
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glPopMatrix()
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
        GL11.glPopMatrix()
        GL11.glPopAttrib()
    }

	override fun onEntityRender(entity: EntityLivingBase, color: Color) {
        val posX = MathUtils.interpolate(entity.posX, entity.lastTickPosX, mc.timer.renderPartialTicks)
        val posY = MathUtils.interpolate(entity.posY, entity.lastTickPosY, mc.timer.renderPartialTicks)
        val posZ = MathUtils.interpolate(entity.posZ, entity.lastTickPosZ, mc.timer.renderPartialTicks)

        val bb = entity.entityBoundingBox
            .offset(-entity.posX, -entity.posY, -entity.posZ)
            .offset(posX, posY, posZ)
            .offset(-mc.renderManager.renderPosX, -mc.renderManager.renderPosY, -mc.renderManager.renderPosZ)
        
        val boxVertices = arrayOf(
            Vector3f(bb.minX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()),
            Vector3f(bb.minX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()),
            Vector3f(bb.maxX.toFloat(), bb.maxY.toFloat(), bb.minZ.toFloat()),
            Vector3f(bb.maxX.toFloat(), bb.minY.toFloat(), bb.minZ.toFloat()),
            Vector3f(bb.minX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat()),
            Vector3f(bb.minX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()),
            Vector3f(bb.maxX.toFloat(), bb.maxY.toFloat(), bb.maxZ.toFloat()),
            Vector3f(bb.maxX.toFloat(), bb.minY.toFloat(), bb.maxZ.toFloat())
        )

        var minX = mc.displayWidth.toFloat()
        var minY = mc.displayHeight.toFloat()
        var maxX = 0f
        var maxY = 0f
        for (boxVertex in boxVertices) {
            val screenPos = WorldToScreen.worldToScreen(boxVertex, mvMatrix, projectionMatrix, mc.displayWidth, mc.displayHeight) ?: continue
            
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
            entities.add(entity)

            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            val stringWidth = mc.fontRendererObj.getStringWidth(entity.displayName.formattedText)
            mc.fontRendererObj.drawStringWithShadow(entity.displayName.formattedText, minX + (maxX - minX) / 2 - stringWidth / 2, minY - 12, -1)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
        }
    }

    override fun onRenderNameTags(event: RenderNameTagsEvent) {
        if (event.entity in entities)
            event.isCancelled = true
    }
}