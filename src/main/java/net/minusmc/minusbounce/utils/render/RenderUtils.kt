/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render

import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.modules.render.TargetMark
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minusmc.minusbounce.utils.extensions.step
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.utils.render.ColorUtils.setColour
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.*


object RenderUtils : MinecraftInstance() {
    private val DISPLAY_LISTS_2D = (0..3).map {glGenLists(1)}.toTypedArray()
    private const val ANIMATION_DURATION = 500
    private var startTime = 0L
    var deltaTime = 0

    private val frustrum = Frustum()
    private var zLevel = 0f

    init {
        glNewList(DISPLAY_LISTS_2D[0], GL_COMPILE)
        quickDrawRect(-7f, 2f, -4f, 3f)
        quickDrawRect(4f, 2f, 7f, 3f)
        quickDrawRect(-7f, 0.5f, -6f, 3f)
        quickDrawRect(6f, 0.5f, 7f, 3f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[1], GL_COMPILE)
        quickDrawRect(-7f, 3f, -4f, 3.3f)
        quickDrawRect(4f, 3f, 7f, 3.3f)
        quickDrawRect(-7.3f, 0.5f, -7f, 3.3f)
        quickDrawRect(7f, 0.5f, 7.3f, 3.3f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[2], GL_COMPILE)
        quickDrawRect(4f, -20f, 7f, -19f)
        quickDrawRect(-7f, -20f, -4f, -19f)
        quickDrawRect(6f, -20f, 7f, -17.5f)
        quickDrawRect(-7f, -20f, -6f, -17.5f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[3], GL_COMPILE)
        quickDrawRect(7f, -20f, 7.3f, -17.5f)
        quickDrawRect(-7.3f, -20f, -7f, -17.5f)
        quickDrawRect(4f, -20.3f, 7.3f, -20f)
        quickDrawRect(-7.3f, -20.3f, -4f, -20f)
        glEndList()
    }

    fun drawRoundedCornerRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float, color: Int) {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_TEXTURE_2D)
        val hasCull = glIsEnabled(GL_CULL_FACE)
        glDisable(GL_CULL_FACE)
        glColor(color)
        drawRoundedCornerRect(x, y, x1, y1, radius)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        GLUtils.setGlState(GL_CULL_FACE, hasCull)
    }

    fun drawRoundedCornerRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float) {
        glBegin(GL_POLYGON)
        val xRadius = ((x1 - x) * 0.5).coerceAtMost(radius.toDouble()).toFloat()
        val yRadius = ((y1 - y) * 0.5).coerceAtMost(radius.toDouble()).toFloat()
        quickPolygonCircle(x + xRadius, y + yRadius, xRadius, yRadius, 180, 270)
        quickPolygonCircle(x1 - xRadius, y + yRadius, xRadius, yRadius, 90, 180)
        quickPolygonCircle(x1 - xRadius, y1 - yRadius, xRadius, yRadius, 0, 90)
        quickPolygonCircle(x + xRadius, y1 - yRadius, xRadius, yRadius, 270, 360)
        glEnd()
    }

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
     */

    fun drawSquareTriangle(cx: Float, cy: Float, dirX: Float, dirY: Float, color: Color, filled: Boolean) {
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.resetColor()
        glColor(color)
        worldrenderer.begin(if (filled) 5 else 2, DefaultVertexFormats.POSITION)
        worldrenderer.pos((cx + dirX).toDouble(), cy.toDouble(), 0.0).endVertex()
        worldrenderer.pos(cx.toDouble(), cy.toDouble(), 0.0).endVertex()
        worldrenderer.pos(cx.toDouble(), (cy + dirY).toDouble(), 0.0).endVertex()
        worldrenderer.pos((cx + dirX).toDouble(), cy.toDouble(), 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    fun drawTexturedModalRect(x: Int, y: Int, textureX: Int, textureY: Int, width: Int, height: Int, zLevel: Float) {
        val f = 0.00390625f
        val f1 = 0.00390625f
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos((x + 0).toDouble(), (y + height).toDouble(), zLevel.toDouble())
            .tex(((textureX + 0).toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble()).endVertex()
        worldRenderer.pos((x + width).toDouble(), (y + height).toDouble(), zLevel.toDouble())
            .tex(((textureX + width).toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble())
            .endVertex()
        worldRenderer.pos((x + width).toDouble(), (y + 0).toDouble(), zLevel.toDouble())
            .tex(((textureX + width).toFloat() * f).toDouble(), ((textureY + 0).toFloat() * f1).toDouble()).endVertex()
        worldRenderer.pos((x + 0).toDouble(), (y + 0).toDouble(), zLevel.toDouble())
            .tex(((textureX + 0).toFloat() * f).toDouble(), ((textureY + 0).toFloat() * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    fun drawHead(skin: ResourceLocation, x: Int, y: Int, width: Int, height: Int) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(skin)
        Gui.drawScaledCustomSizeModalRect(x, y, 8F, 8F, 8, 8, width, height, 64F, 64F)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun isInViewFrustrum(entity: Entity) = isInViewFrustrum(entity.entityBoundingBox) || entity.ignoreFrustumCheck

    fun isInViewFrustrum(bb: AxisAlignedBB): Boolean {
        val current = mc.renderViewEntity
        frustrum.setPosition(current.posX, current.posY, current.posZ)
        return frustrum.isBoundingBoxInFrustum(bb)
    }

    fun drawExhiRect(x: Float, y: Float, x2: Float, y2: Float) {
        drawRect(x - 3.5f, y - 3.5f, x2 + 3.5f, y2 + 3.5f, Color.black.rgb)
        drawRect(x - 3f, y - 3f, x2 + 3f, y2 + 3f, Color(50, 50, 50).rgb)
        drawRect(x - 2.5f, y - 2.5f, x2 + 2.5f, y2 + 2.5f, Color(26, 26, 26).rgb)
        drawRect(x - 0.5f, y - 0.5f, x2 + 0.5f, y2 + 0.5f, Color(50, 50, 50).rgb)
        drawRect(x, y, x2, y2, Color(18, 18, 18).rgb)
    }

    fun drawExhiRect(x: Float, y: Float, x2: Float, y2: Float, alpha: Float) {
        drawRect(x - 3.5f, y - 3.5f, x2 + 3.5f, y2 + 3.5f, Color(0f, 0f, 0f, alpha).rgb)
        drawRect(x - 3f, y - 3f, x2 + 3f, y2 + 3f, Color(50f / 255f, 50f / 255f, 50f / 255f, alpha).rgb)
        drawRect(x - 2.5f, y - 2.5f, x2 + 2.5f, y2 + 2.5f, Color(26f / 255f, 26f / 255f, 26f / 255f, alpha).rgb)
        drawRect(x - 0.5f, y - 0.5f, x2 + 0.5f, y2 + 0.5f, Color(50f / 255f, 50f / 255f, 50f / 255f, alpha).rgb)
        drawRect(x, y, x2, y2, Color(18f / 255f, 18 / 255f, 18f / 255f, alpha).rgb)
    }

    fun drawMosswareRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int) {
        drawRect(x, y, x2, y2, color2)
        drawBorder(x, y, x2, y2, width, color1)
    }

    fun originalRoundedRect(paramXStart: Float, paramYStart: Float, paramXEnd: Float, paramYEnd: Float, radius: Float, color: Int) {
        val (xStart, xEnd) = if (paramXStart > paramXEnd) paramXEnd to paramXStart else paramXStart to paramXEnd
        val (yStart, yEnd) = if (paramYStart > paramYEnd) paramYEnd to paramYStart else paramYStart to paramYEnd

        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        val x1 = (xStart + radius).toDouble()
        val y1 = (yStart + radius).toDouble()
        val x2 = (xEnd - radius).toDouble()
        val y2 = (yEnd - radius).toDouble()
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(red, green, blue, alpha)
        worldrenderer.begin(GL_POLYGON, DefaultVertexFormats.POSITION)
        val degree = PI / 180

        for (i in 0.0..90.0 step 1.0)
            worldrenderer.pos(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius, 0.0).endVertex()

        for (i in 90.0..180.0 step 1.0)
            worldrenderer.pos(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius, 0.0).endVertex()

        for (i in 180.0..270.0 step 1.0)
            worldrenderer.pos(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius, 0.0).endVertex()

        for (i in 270.0..360.0 step 1.0)
            worldrenderer.pos(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius, 0.0).endVertex()

        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun newDrawRect(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        newDrawRect(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble(), color)
    }

    fun newDrawRect(pLeft: Double, pTop: Double, pRight: Double, pBottom: Double, color: Int) {
        val (left, right) = if (pLeft > pRight) pRight to pLeft else pLeft to pRight
        val (top, bottom) = if (pTop > pBottom) pBottom to pTop else pTop to pBottom

        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left, bottom, 0.0).endVertex()
        worldrenderer.pos(right, bottom, 0.0).endVertex()
        worldrenderer.pos(right, top, 0.0).endVertex()
        worldrenderer.pos(left, top, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    @JvmOverloads
    fun drawRoundedRectWithWidthHeight(x: Number, y: Number, width: Number, height: Number, radius: Number, color: Int, popPush: Boolean = true) {
        drawRoundedRect(x.toDouble(), y.toDouble(), x.toDouble() + width.toDouble(), y.toDouble() + height.toDouble(), radius.toDouble(), color, popPush)
    }

    @JvmOverloads
    fun drawRoundedRect(paramXStart: Number, paramYStart: Number, paramXEnd: Number, paramYEnd: Number, radius: Number, color: Int, popPush: Boolean = true) {
        drawRoundedRect(paramXStart.toDouble(), paramYStart.toDouble(), paramXEnd.toDouble(), paramYEnd.toDouble(), radius.toDouble(), color, popPush)
    }

    fun drawRoundedRect(paramXStart: Double, paramYStart: Double, paramXEnd: Double, paramYEnd: Double, radius: Double, color: Int, popPush: Boolean = true) {
        val (xStart, xEnd) = if (paramXStart > paramXEnd) paramXEnd to paramXStart else paramXStart to paramXEnd
        val (yStart, yEnd) = if (paramYStart > paramYEnd) paramYEnd to paramYStart else paramYStart to paramYEnd

        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        val x1 = xStart + radius
        val y1 = yStart + radius
        val x2 = xEnd - radius
        val y2 = yEnd - radius

        if (popPush)
            glPushMatrix()

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)
        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)
        val degree = PI / 180

        for (i in 0.0..90.0 step 1.0)
            glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)

        for (i in 90.0..180.0 step 1.0)
            glVertex2d(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)

        for (i in 180.0..270.0 step 1.0)
            glVertex2d(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)

        for (i in 270.0..360.0 step 1.0)
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)

        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        if (popPush) glPopMatrix()
    }

    fun drawScaledCustomSizeModalRect(x: Int, y: Int, u: Float, v: Float, uWidth: Int, vHeight: Int, width: Int, height: Int, tileWidth: Float, tileHeight: Float) {
        val f = 1.0f / tileWidth
        val f1 = 1.0f / tileHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + uWidth.toFloat()) * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + uWidth.toFloat()) * f).toDouble(), (v * f1).toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    // rTL = radius top left, rTR = radius top right, rBR = radius bottom right, rBL = radius bottom left
    fun customRounded(paramXStart: Double,paramYStart: Double,paramXEnd: Double,paramYEnd: Double,rTL: Double,rTR: Double,rBR: Double,rBL: Double,color: Int) {
        val (xStart, xEnd) = if (paramXStart > paramXEnd) paramXEnd to paramXStart else paramXStart to paramXEnd
        val (yStart, yEnd) = if (paramYStart > paramYEnd) paramYEnd to paramYStart else paramYStart to paramYEnd

        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        val xTL = xStart + rTL
        val yTL = yStart + rTL
        val xTR = xEnd - rTR
        val yTR = yStart + rTR
        val xBR = xEnd - rBR
        val yBR = yEnd - rBR
        val xBL = xStart + rBL
        val yBL = yEnd - rBL

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)
        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)
        val degree = PI / 180



        if (rBR <= 0)
            glVertex2d(xBR, yBR)
        else for (i in 0.0..90.0 step 1.0)
            glVertex2d(xBR + sin(i * degree) * rBR, yBR + cos(i * degree) * rBR)

        if (rTR <= 0)
            glVertex2d(xTR, yTR)
        else for (i in 90.0..180.0 step 1.0)
            glVertex2d(xTR + sin(i * degree) * rTR, yTR + cos(i * degree) * rTR)

        if (rTL <= 0)
            glVertex2d(xTL, yTL)
        else for (i in 180.0..270.0 step 1.0)
            glVertex2d(xTL + sin(i * degree) * rTL, yTL + cos(i * degree) * rTL)

        if (rBL <= 0)
            glVertex2d(xBL, yBL)
        else for (i in 270.0..360.0 step 1.0)
            glVertex2d(xBL + sin(i * degree) * rBL, yBL + cos(i * degree) * rBL)

        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    fun fastRoundedRect(paramXStart: Number, paramYStart: Number, paramXEnd: Number, paramYEnd: Number, radius: Number) {
        fastRoundedRect(paramXStart.toDouble(), paramYStart.toDouble(), paramXEnd.toDouble(), paramYEnd.toDouble(), radius.toDouble())
    }

    fun fastRoundedRect(paramXStart: Double, paramYStart: Double, paramXEnd: Double, paramYEnd: Double, radius: Double) {
        val (xStart, xEnd) = if (paramXStart > paramXEnd) paramXEnd to paramXStart else paramXStart to paramXEnd
        val (yStart, yEnd) = if (paramYStart > paramYEnd) paramYEnd to paramYStart else paramYStart to paramYEnd

        val x1 = xStart + radius
        val y1 = yStart + radius
        val x2 = xEnd - radius
        val y2 = yEnd - radius

        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)
        glBegin(GL_POLYGON)
        val degree = Math.PI / 180

        for (i in 0.0..90.0 step 1.0)
            glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)

        for (i in 90.0..180.0 step 1.0)
            glVertex2d(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)

        for (i in 180.0..270.0 step 1.0)
            glVertex2d(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)

        for (i in 270.0..360.0 step 1.0)
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)


        glEnd()
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawTriangle(paramX: Float, paramY: Float, radius: Float, n: Float, color: Color, polygon: Boolean) {
        var cx = paramX
        var cy = paramY
        var r = radius
        cx *= 2.0.toFloat()
        cy *= 2.0.toFloat()
        val b = 6.2831852 / n
        val p = cos(b)
        val s = sin(b)
        r *= 2.0.toFloat()
        var x = r.toDouble()
        var y = 0.0
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        glLineWidth(1f)
        GLUtils.enableGlCap(GL_LINE_SMOOTH)
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.resetColor()
        glColor(color)
        GlStateManager.scale(0.5f, 0.5f, 0.5f)
        worldrenderer.begin(if (polygon) GL_POLYGON else 2, DefaultVertexFormats.POSITION)
        var ii = 0
        while (ii < n) {
            worldrenderer.pos(x + cx, y + cy, 0.0).endVertex()
            val t = x
            x = p * x - s * y
            y = s * t + p * y
            ii++
        }
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.scale(2f, 2f, 2f)
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    fun drawGradientRect(left: Int, top: Int, right: Int, bottom: Int, startColor: Int, endColor: Int) {
        val f = (startColor shr 24 and 255).toFloat() / 255.0f
        val f1 = (startColor shr 16 and 255).toFloat() / 255.0f
        val f2 = (startColor shr 8 and 255).toFloat() / 255.0f
        val f3 = (startColor and 255).toFloat() / 255.0f
        val f4 = (endColor shr 24 and 255).toFloat() / 255.0f
        val f5 = (endColor shr 16 and 255).toFloat() / 255.0f
        val f6 = (endColor shr 8 and 255).toFloat() / 255.0f
        val f7 = (endColor and 255).toFloat() / 255.0f
        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(right.toDouble(), top.toDouble(), zLevel.toDouble()).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), zLevel.toDouble()).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), zLevel.toDouble()).color(f5, f6, f7, f4).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), zLevel.toDouble()).color(f5, f6, f7, f4).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    fun drawGradientSideways(left: Float, top: Float, right: Float, bottom: Float, col1: Int, col2: Int) {
        val f = (col1 shr 24 and 0xFF) / 255.0f
        val f2 = (col1 shr 16 and 0xFF) / 255.0f
        val f3 = (col1 shr 8 and 0xFF) / 255.0f
        val f4 = (col1 and 0xFF) / 255.0f
        val f5 = (col2 shr 24 and 0xFF) / 255.0f
        val f6 = (col2 shr 16 and 0xFF) / 255.0f
        val f7 = (col2 shr 8 and 0xFF) / 255.0f
        val f8 = (col2 and 0xFF) / 255.0f
        glEnable(3042)
        glDisable(3553)
        glBlendFunc(770, 771)
        glEnable(2848)
        glShadeModel(7425)
        glPushMatrix()
        glBegin(7)
        glColor4f(f2, f3, f4, f)
        glVertex2f(left, top)
        glVertex2f(left, bottom)
        glColor4f(f6, f7, f8, f5)
        glVertex2f(right, bottom)
        glVertex2f(right, top)
        glEnd()
        glPopMatrix()
        glEnable(3553)
        glDisable(3042)
        glDisable(2848)
        glShadeModel(7424)
    }

    fun drawBlockBox(blockPos: BlockPos, color: Color, outline: Boolean) {
        val renderManager = mc.renderManager
        val timer = mc.timer
        val x = blockPos.x - renderManager.renderPosX
        val y = blockPos.y - renderManager.renderPosY
        val z = blockPos.z - renderManager.renderPosZ
        var axisAlignedBB = AxisAlignedBB(x, y, z, x + 1.0, y + 1, z + 1.0)
        val block = BlockUtils.getBlock(blockPos)
        if (block != null) {
            val player: EntityPlayer = mc.thePlayer
            val posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * timer.renderPartialTicks.toDouble()
            val posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * timer.renderPartialTicks.toDouble()
            val posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * timer.renderPartialTicks.toDouble()
            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
                .offset(-posX, -posY, -posZ)
        }
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GLUtils.enableGlCap(GL_BLEND)
        GLUtils.disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color.red, color.green, color.blue, if (color.alpha != 255) color.alpha else if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)
        if (outline) {
            glLineWidth(1f)
            GLUtils.enableGlCap(GL_LINE_SMOOTH)
            glColor(color)
            drawSelectionBoundingBox(axisAlignedBB)
        }
        GlStateManager.resetColor()
        glDepthMask(true)
        GLUtils.resetCaps()
    }

    fun drawShadow(x: Float, y: Float, width: Float, height: Float) {
        drawTexturedRect(x - 9, y - 9, 9f, 9f, "paneltopleft")
        drawTexturedRect(x - 9, y + height, 9f, 9f, "panelbottomleft")
        drawTexturedRect(x + width, y + height, 9f, 9f, "panelbottomright")
        drawTexturedRect(x + width, y - 9, 9f, 9f, "paneltopright")
        drawTexturedRect(x - 9, y, 9f, height, "panelleft")
        drawTexturedRect(x + width, y, 9f, height, "panelright")
        drawTexturedRect(x, y - 9, width, 9f, "paneltop")
        drawTexturedRect(x, y + height, width, 9f, "panelbottom")
    }

    fun drawTexturedRect(x: Float, y: Float, width: Float, height: Float, image: String) {
        glPushMatrix()
        val enableBlend = glIsEnabled(GL_BLEND)
        val disableAlpha = !glIsEnabled(GL_ALPHA_TEST)
        if (!enableBlend) glEnable(GL_BLEND)
        if (!disableAlpha) glDisable(GL_ALPHA_TEST)
        mc.textureManager.bindTexture(ResourceLocation("liquidbounce+/ui/$image.png"))
        GlStateManager.color(1f, 1f, 1f, 1f)
        drawModalRectWithCustomSizedTexture(x.toInt(), y.toInt(), 0f, 0f, width.toInt(), height.toInt(), width, height)
        if (!enableBlend) glDisable(GL_BLEND)
        if (!disableAlpha) glEnable(GL_ALPHA_TEST)
        glPopMatrix()
    }

    fun drawSelectionBoundingBox(boundingBox: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)

        // Lower Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        tessellator.draw()
    }

    fun drawEntityBox(entity: Entity, color: Color, outline: Boolean) {
        val renderManager = mc.renderManager
        val timer = mc.timer
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GLUtils.enableGlCap(GL_BLEND)
        GLUtils.disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)
        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ)
        val entityBox = entity.entityBoundingBox
        val axisAlignedBB = AxisAlignedBB(
            entityBox.minX - entity.posX + x - 0.05,
            entityBox.minY - entity.posY + y,
            entityBox.minZ - entity.posZ + z - 0.05,
            entityBox.maxX - entity.posX + x + 0.05,
            entityBox.maxY - entity.posY + y + 0.15,
            entityBox.maxZ - entity.posZ + z + 0.05
        )
        if (outline) {
            glLineWidth(1f)
            GLUtils.enableGlCap(GL_LINE_SMOOTH)
            glColor(color.red, color.green, color.blue, 95)
            drawSelectionBoundingBox(axisAlignedBB)
        }
        glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)
        GlStateManager.resetColor()
        glDepthMask(true)
        GLUtils.resetCaps()
    }

    fun drawAxisAlignedBB(axisAlignedBB: AxisAlignedBB, color: Color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glLineWidth(2f)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color)
        drawFilledBox(axisAlignedBB)
        GlStateManager.resetColor()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    fun drawPlatform(y: Double, color: Color, size: Double) {
        val renderManager = mc.renderManager
        val renderY = y - renderManager.renderPosY
        drawAxisAlignedBB(AxisAlignedBB(size, renderY + 0.02, size, -size, renderY, -size), color)
    }

    fun drawPlatform(entity: Entity, color: Color) {
        val renderManager = mc.renderManager
        val timer = mc.timer
        val targetMark = MinusBounce.moduleManager.getModule(TargetMark::class.java) ?: return
        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ)
        val axisAlignedBB = entity.entityBoundingBox
            .offset(-entity.posX, -entity.posY, -entity.posZ)
            .offset(x, y - targetMark.moveMarkValue.get(), z)
        drawAxisAlignedBB(
            AxisAlignedBB(
                axisAlignedBB.minX,
                axisAlignedBB.maxY + 0.2,
                axisAlignedBB.minZ,
                axisAlignedBB.maxX,
                axisAlignedBB.maxY + 0.26,
                axisAlignedBB.maxZ
            ),
            color
        )
    }

    fun drawFilledBox(axisAlignedBB: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        tessellator.draw()
    }

    fun drawEntityOnScreen(posX: Double, posY: Double, scale: Float, entity: EntityLivingBase?) {
        GlStateManager.pushMatrix()
        GlStateManager.enableColorMaterial()
        GlStateManager.translate(posX, posY, 50.0)
        GlStateManager.scale(-scale, scale, scale)
        GlStateManager.rotate(180f, 0f, 0f, 1f)
        GlStateManager.rotate(135f, 0f, 1f, 0f)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.rotate(-135f, 0f, 1f, 0f)
        GlStateManager.translate(0.0, 0.0, 0.0)
        val rendermanager = mc.renderManager
        rendermanager.setPlayerViewY(180f)
        rendermanager.isRenderShadow = false
        rendermanager.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0f, 1f)
        rendermanager.isRenderShadow = true
        GlStateManager.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
    }

    fun drawEntityOnScreen(posX: Int, posY: Int, scale: Int, entity: EntityLivingBase?) {
        drawEntityOnScreen(posX.toDouble(), posY.toDouble(), scale.toFloat(), entity)
    }

    fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float) {
        glBegin(GL_QUADS)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
    }

    fun drawRect(x: Number, y: Number, x2: Number, y2: Number, color: Int) {
        drawRect(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), color)
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        glBegin(GL_QUADS)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    fun drawRect(pLeft: Double, pTop: Double, pRight: Double, pBottom: Double, color: Int) {
        val (left, right) = if (pLeft > pRight) pRight to pLeft else pLeft to pRight
        val (top, bottom) = if (pTop > pBottom) pBottom to pTop else pTop to pBottom

        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left, bottom, 0.0).endVertex()
        worldrenderer.pos(right, bottom, 0.0).endVertex()
        worldrenderer.pos(right, top, 0.0).endVertex()
        worldrenderer.pos(left, top, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    /**
     * Like [.drawRect], but without setup
     */
    fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        glColor(color)
        glBegin(GL_QUADS)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Color) {
        drawRect(x, y, x2, y2, color.rgb)
    }

    fun drawBorderedRect(x: Number, y: Number, x2: Number, y2: Number, width: Number, color1: Int, color2: Int) {
        drawBorderedRect(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), width.toFloat(), color1, color2)
    }

    fun drawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int) {
        drawRect(x, y, x2, y2, color2)
        drawBorder(x, y, x2, y2, width, color1)
    }

    fun drawBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color1)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawRectBasedBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int) {
        drawRect(x - width / 2f, y - width / 2f, x2 + width / 2f, y + width / 2f, color1)
        drawRect(x - width / 2f, y + width / 2f, x + width / 2f, y2 + width / 2f, color1)
        drawRect(x2 - width / 2f, y + width / 2f, x2 + width / 2f, y2 + width / 2f, color1)
        drawRect(x + width / 2f, y2 - width / 2f, x2 - width / 2f, y2 + width / 2f, color1)
    }

    fun drawRectBasedBorder(x: Double, y: Double, x2: Double, y2: Double, width: Double, color1: Int) {
        newDrawRect(x - width / 2f, y - width / 2f, x2 + width / 2f, y + width / 2f, color1)
        newDrawRect(x - width / 2f, y + width / 2f, x + width / 2f, y2 + width / 2f, color1)
        newDrawRect(x2 - width / 2f, y + width / 2f, x2 + width / 2f, y2 + width / 2f, color1)
        newDrawRect(x + width / 2f, y2 - width / 2f, x2 - width / 2f, y2 + width / 2f, color1)
    }

    fun quickDrawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int) {
        quickDrawRect(x, y, x2, y2, color2)
        glColor(color1)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
    }

    fun drawImage(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun drawImage(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int, alpha: Float) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1.0f, 1.0f, 1.0f, alpha)
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun drawImagee(image: ResourceLocation?, x: Double, y: Double, width: Double, height: Double) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(x.toInt(),
            y.toInt(), 0f, 0f, width.toInt(), height.toInt(), width.toFloat(), height.toFloat())
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun drawImagee(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int, alpha: Float) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1.0f, 1.0f, 1.0f, alpha)
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun drawImage2(image: ResourceLocation?, x: Float, y: Float, width: Int, height: Int) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        glTranslatef(x, y, x)
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        glTranslatef(-x, -y, -x)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun drawImage3(
        image: ResourceLocation?,
        x: Float,
        y: Float,
        width: Int,
        height: Int,
        r: Float,
        g: Float,
        b: Float,
        al: Float
    ) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(r, g, b, al)
        glTranslatef(x, y, x)
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        glTranslatef(-x, -y, -x)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun drawExhiEnchants(stack: ItemStack, x: Int, y: Int) {
        drawExhiEnchants(stack, x.toFloat(), y.toFloat())
    }

    fun drawExhiEnchants(stack: ItemStack, x: Float, y: Float) {
        var yHeight = y
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()
        val darkBorder = -0x1000000

        if (stack.item is ItemArmor) {
            val prot = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            val thorn = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack)
            if (prot > 0) {
                drawExhiOutlined(prot.toString(), drawExhiOutlined("P", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(prot), getMainColor(prot), true)
                yHeight += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(unb.toString(), drawExhiOutlined("U", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(unb), getMainColor(unb), true)
                yHeight += 4f
            }
            if (thorn > 0) {
                drawExhiOutlined(thorn.toString(), drawExhiOutlined("T", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(thorn), getMainColor(thorn), true)
                yHeight += 4f
            }
        }
        if (stack.item is ItemBow) {
            val power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack)
            val punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack)
            val flame = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (power > 0) {
                drawExhiOutlined(power.toString(), drawExhiOutlined("Pow", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(power), getMainColor(power), true)
                yHeight += 4f
            }
            if (punch > 0) {
                drawExhiOutlined(punch.toString(), drawExhiOutlined("Pun", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(punch), getMainColor(punch), true)
                yHeight += 4f
            }
            if (flame > 0) {
                drawExhiOutlined(flame.toString(), drawExhiOutlined("F", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(flame), getMainColor(flame), true)
                yHeight += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(unb.toString(), drawExhiOutlined("U", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(unb), getMainColor(unb), true)
                yHeight += 4f
            }
        }
        if (stack.item is ItemSword) {
            val sharp = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack)
            val kb = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack)
            val fire = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (sharp > 0) {
                drawExhiOutlined(sharp.toString(), drawExhiOutlined("S", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(sharp), getMainColor(sharp), true)
                yHeight += 4f
            }
            if (kb > 0) {
                drawExhiOutlined(kb.toString(), drawExhiOutlined("K", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(kb), getMainColor(kb), true)
                yHeight += 4f
            }
            if (fire > 0) {
                drawExhiOutlined(fire.toString(), drawExhiOutlined("F", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(fire), getMainColor(fire), true)
                yHeight += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(unb.toString(), drawExhiOutlined("U", x, yHeight, 0.35f, darkBorder, -1, true), yHeight, 0.35f, getBorderColor(unb), getMainColor(unb), true)
                yHeight += 4f
            }
        }
        GlStateManager.enableDepth()
        RenderHelper.enableGUIStandardItemLighting()
    }

    private fun drawExhiOutlined(text: String, x: Float, y: Float, borderWidth: Float, borderColor: Int, mainColor: Int, drawText: Boolean): Float {
        Fonts.fontTahomaSmall.drawString(text, x, y - borderWidth, borderColor)
        Fonts.fontTahomaSmall.drawString(text, x, y + borderWidth, borderColor)
        Fonts.fontTahomaSmall.drawString(text, x - borderWidth, y, borderColor)
        Fonts.fontTahomaSmall.drawString(text, x + borderWidth, y, borderColor)
        if (drawText) Fonts.fontTahomaSmall.drawString(text, x, y, mainColor)
        return x + Fonts.fontTahomaSmall.getWidth(text) - 2f
    }

    private fun getMainColor(level: Int): Int {
        return if (level == 4) -0x560000 else -1
    }

    private fun getBorderColor(level: Int): Int {
        if (level == 2) return 0x7055FF55
        if (level == 3) return 0x7000AAAA
        if (level == 4) return 0x70AA0000
        return if (level >= 5) 0x70FFAA00 else 0x70FFFFFF
    }

    fun glColor(red: Int, green: Int, blue: Int, alpha: Int) {
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
    }

    fun glColor(color: Color) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f
        GlStateManager.color(red, green, blue, alpha)
    }

    fun glColor(color: Color, alpha: Float) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        GlStateManager.color(red, green, blue, alpha / 255f)
    }

    fun glColor(color: Int) {
        val alpha = (color shr 24 and 0xFF) / 255f
        val red = (color shr 16 and 0xFF) / 255f
        val green = (color shr 8 and 0xFF) / 255f
        val blue = (color and 0xFF) / 255f
        GlStateManager.color(red, green, blue, alpha)
    }

    fun draw2D(entity: EntityLivingBase, posX: Double, posY: Double, posZ: Double, color: Int, backgroundColor: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(posX, posY, posZ)
        GlStateManager.rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        GlStateManager.scale(-0.1, -0.1, 0.1)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.depthMask(true)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[0])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[1])
        GlStateManager.translate(0.0, 21 + -(entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) * 12, 0.0)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[2])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[3])

        // Stop render
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        GlStateManager.popMatrix()
    }

    fun draw2D(blockPos: BlockPos, color: Int, backgroundColor: Int) {
        val renderManager = mc.renderManager
        val posX = blockPos.x + 0.5 - renderManager.renderPosX
        val posY = blockPos.y - renderManager.renderPosY
        val posZ = blockPos.z + 0.5 - renderManager.renderPosZ
        GlStateManager.pushMatrix()
        GlStateManager.translate(posX, posY, posZ)
        GlStateManager.rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        GlStateManager.scale(-0.1, -0.1, 0.1)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.depthMask(true)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[0])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[1])
        GlStateManager.translate(0f, 9f, 0f)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[2])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[3])

        // Stop render
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        GlStateManager.popMatrix()
    }

    fun renderNameTag(string: String?, x: Double, y: Double, z: Double) {
        val renderManager = mc.renderManager
        glPushMatrix()
        glTranslated(x - renderManager.renderPosX, y - renderManager.renderPosY, z - renderManager.renderPosZ)
        glNormal3f(0f, 1f, 0f)
        glRotatef(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        glRotatef(mc.renderManager.playerViewX, 1f, 0f, 0f)
        glScalef(-0.05f, -0.05f, 0.05f)
        GLUtils.setGlCap(GL_LIGHTING, false)
        GLUtils.setGlCap(GL_DEPTH_TEST, false)
        GLUtils.setGlCap(GL_BLEND, true)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        val width = Fonts.font35.getStringWidth(string!!) / 2
        Gui.drawRect(-width - 1, -1, width + 1, Fonts.font35.FONT_HEIGHT, Int.MIN_VALUE)
        Fonts.font35.drawString(string, -width.toFloat(), 1.5f, Color.WHITE.rgb, true)
        GLUtils.resetCaps()
        glColor4f(1f, 1f, 1f, 1f)
        glPopMatrix()
    }

    fun drawLine(x: Float, y: Float, x1: Float, y1: Float, width: Float) {
        glDisable(GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL_LINES)
        glVertex2f(x, y)
        glVertex2f(x1, y1)
        glEnd()
        glEnable(GL_TEXTURE_2D)
    }

    fun drawLimitedCircle(lx: Float, ly: Float, x2: Float, y2: Float, xx: Int, yy: Int, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float
        glPushAttrib(GL_ENABLE_BIT)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_TRIANGLE_FAN)
        for (i in 0 until sections) {
            x = (radius * sin(i * dAngle)).toFloat()
            y = (radius * cos(i * dAngle)).toFloat()
            glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            glVertex2f(
                min(x2.toDouble(), max((xx + x).toDouble(), lx.toDouble())).toFloat(),
                min(y2.toDouble(), max((yy + y).toDouble(), ly.toDouble())).toFloat()
            )
        }
        GlStateManager.color(0f, 0f, 0f)
        glEnd()
        glPopAttrib()
    }

    fun drawLine(x: Double, y: Double, x1: Double, y1: Double, width: Float) {
        glDisable(GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL_LINES)
        glVertex2d(x, y)
        glVertex2d(x1, y1)
        glEnd()
        glEnable(GL_TEXTURE_2D)
    }

    fun makeScissorBox(x: Float, y: Float, x2: Float, y2: Float) {
        val scaledResolution = ScaledResolution(mc)
        val factor = scaledResolution.scaleFactor
        glScissor(
            (x * factor).toInt(),
            ((scaledResolution.scaledHeight - y2) * factor).toInt(),
            ((x2 - x) * factor).toInt(),
            ((y2 - y) * factor).toInt()
        )
    }
    fun otherDrawOutlinedBoundingBox(pYaw: Float, x: Double, y: Double, z: Double, pWidth: Double, height: Double) {
        val width = pWidth * 1.5
        var yaw = MathUtils.wrapAngleTo180(pYaw) + 45f

        val yawRender1 = if (yaw < 0.0) {
            MathUtils.toRadians(abs(yaw) - 360f)
        } else {
            MathUtils.toRadians(yaw)
        }
        yaw += 90.0F

        val yawRender2 = if (yaw < 0.0) {
            MathUtils.toRadians(abs(yaw) - 360f)
        } else {
            MathUtils.toRadians(yaw)
        }
        yaw += 90.0F

        val yawRender3 = if (yaw < 0.0) {
            MathUtils.toRadians(abs(yaw) - 360f)
        } else {
            MathUtils.toRadians(yaw)
        }
        yaw += 90.0F

        val yawRender4 = if (yaw < 0.0) {
            MathUtils.toRadians(abs(yaw) - 360f)
        } else {
            MathUtils.toRadians(yaw)
        }

        val x1 = (sin(yawRender1) * width + x).toFloat()
        val z1 = (cos(yawRender1) * width + z).toFloat()
        val x2 = (sin(yawRender2) * width + x).toFloat()
        val z2 = (cos(yawRender2) * width + z).toFloat()
        val x3 = (sin(yawRender3) * width + x).toFloat()
        val z3 = (cos(yawRender3) * width + z).toFloat()
        val x4 = (sin(yawRender4) * width + x).toFloat()
        val z4 = (cos(yawRender4) * width + z).toFloat()
        val y2 = (y + height).toFloat()

        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldrenderer.pos(x1.toDouble(), y, z1.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y2.toDouble(), z1.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y2.toDouble(), z2.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y, z2.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y, z1.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y, z4.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y, z3.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y2.toDouble(), z3.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y, z4.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y2.toDouble(), z3.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y2.toDouble(), z2.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y, z2.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y, z3.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y, z4.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y2.toDouble(), z1.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y, z1.toDouble()).endVertex()
        tessellator.draw()
    }

    fun drawRoundedGradientOutlineCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        width: Float,
        radius: Float,
        color: Int,
        color2: Int,
        color3: Int,
        color4: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0.toFloat()
        y *= 2.0.toFloat()
        x1 *= 2.0.toFloat()
        y1 *= 2.0.toFloat()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color3)
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        setColour(color4)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        glEnd()
        glLineWidth(1f)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
        setColour(-1)
    }

    fun drawRoundedGradientOutlineCorner(x: Number, y: Number, x1: Number, y1: Number, width: Float, radius: Float, color: Int, color2: Int) {
        drawRoundedGradientOutlineCorner(x.toFloat(), y.toFloat(), x1.toFloat(), y1.toFloat(), width, radius, color, color2)
    }

    fun drawRoundedGradientOutlineCorner(
        px: Float,
        py: Float,
        px1: Float,
        py1: Float,
        width: Float,
        radius: Float,
        color: Int,
        color2: Int
    ) {
        var x = px
        var y = py
        var x1 = px1
        var y1 = py1
        setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0f
        y *= 2.0f
        x1 *= 2.0f
        y1 *= 2.0f
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        glEnd()
        glLineWidth(1f)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
        setColour(-1)
    }

    fun drawRoundedGradientRectCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        radius: Float,
        color: Int,
        color2: Int,
        color3: Int,
        color4: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0.toFloat()
        y *= 2.0.toFloat()
        x1 *= 2.0.toFloat()
        y1 *= 2.0.toFloat()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glBegin(6)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color3)
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        setColour(color4)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
        setColour(-1)
    }

    fun drawAnimatedGradient(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        val currentTime = System.currentTimeMillis()
        if (startTime.toInt() == 0) {
            startTime = currentTime
        }
        val elapsedTime = currentTime - startTime
        val progress: Float = (elapsedTime % ANIMATION_DURATION).toFloat() / ANIMATION_DURATION
        val color1: Int
        val color2: Int
        if ((elapsedTime / ANIMATION_DURATION % 2).toInt() == 0) {
            color1 = interpolateColors(col1, col2, progress)
            color2 = interpolateColors(col2, col1, progress)
        } else {
            color1 = interpolateColors(col2, col1, progress)
            color2 = interpolateColors(col1, col2, progress)
        }
        drawGradientSideways(left, top, right, bottom, color1, color2)
        if (elapsedTime >= 2 * ANIMATION_DURATION) {
            startTime = currentTime
        }
    }

    fun interpolateColors(color1: Int, color2: Int, progress: Float): Int {
        val alpha = ((1.0 - progress) * (color1 ushr 24) + progress * (color2 ushr 24)).toInt()
        val red = ((1.0 - progress) * (color1 shr 16 and 0xFF) + progress * (color2 shr 16 and 0xFF)).toInt()
        val green = ((1.0 - progress) * (color1 shr 8 and 0xFF) + progress * (color2 shr 8 and 0xFF)).toInt()
        val blue = ((1.0 - progress) * (color1 and 0xFF) + progress * (color2 and 0xFF)).toInt()
        return alpha shl 24 or (red shl 16) or (green shl 8) or blue
    }

    fun drawRoundedGradientRectCorner(x: Number, y: Number, x1: Number, y1: Number, radius: Float, color: Int, color2: Int) {
        drawRoundedGradientRectCorner(x.toFloat(), y.toFloat(), x1.toFloat(), y1.toFloat(), radius, color, color2)
    }

    fun drawRoundedGradientRectCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        radius: Float,
        color: Int,
        color2: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        x *= 2.0.toFloat()
        y *= 2.0.toFloat()
        x1 *= 2.0.toFloat()
        y1 *= 2.0.toFloat()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glBegin(6)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 0
        while (i <= 90) {
            glVertex2d(x1 - radius + sin(i * Math.PI / 180.0) * radius, y1 - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
        setColour(-1)
    }

    fun renderHitbox(bb: AxisAlignedBB, type: Int) {
        glBegin(type)

        glVertex3d(bb.minX, bb.minY, bb.maxZ)
        glVertex3d(bb.maxX, bb.minY, bb.maxZ)
        glVertex3d(bb.maxX, bb.minY, bb.minZ)
        glVertex3d(bb.minX, bb.minY, bb.minZ)

        glEnd()

        glBegin(type)

        glVertex3d(bb.minX, bb.maxY, bb.maxZ)
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ)
        glVertex3d(bb.maxX, bb.maxY, bb.minZ)
        glVertex3d(bb.minX, bb.maxY, bb.minZ)

        glEnd()

        glBegin(type)

        glVertex3d(bb.minX, bb.minY, bb.minZ)
        glVertex3d(bb.minX, bb.minY, bb.maxZ)
        glVertex3d(bb.minX, bb.maxY, bb.maxZ)
        glVertex3d(bb.minX, bb.maxY, bb.minZ)

        glEnd()
        glBegin(type)

        glVertex3d(bb.maxX, bb.minY, bb.minZ)
        glVertex3d(bb.maxX, bb.minY, bb.maxZ)
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ)
        glVertex3d(bb.maxX, bb.maxY, bb.minZ)

        glEnd()
        glBegin(type)
        glVertex3d(bb.minX, bb.minY, bb.minZ)
        glVertex3d(bb.maxX, bb.minY, bb.minZ)
        glVertex3d(bb.maxX, bb.maxY, bb.minZ)
        glVertex3d(bb.minX, bb.maxY, bb.minZ)

        glEnd()
        glBegin(type)
        glVertex3d(bb.minX, bb.minY, bb.maxZ)
        glVertex3d(bb.maxX, bb.minY, bb.maxZ)
        glVertex3d(bb.maxX, bb.maxY, bb.maxZ)
        glVertex3d(bb.minX, bb.maxY, bb.maxZ)

        glEnd()
    }

    /**
     * CIRCLE
     */

    fun drawFilledCircleNoGL(x: Int, y: Int, radius: Double, color: Color, quality: Int) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f

        glColor4f(red, green, blue, alpha)
        glBegin(GL_TRIANGLE_FAN)

        for (i in 0 until 360 / quality) {
            val angle = MathUtils.toRadiansFloat(i * quality)

            val x2 = sin(angle) * radius
            val y2 = cos(angle) * radius
            glVertex2d(x + x2, y + y2)
        }
        glEnd()
    }

    fun drawFilledCircleNoGL(x: Int, y: Int, radius: Double, color: Int, quality: Int) {
        val alpha = color shr 24 and 0xFF
        val red = color shr 16 and 0xFF
        val green = color shr 8 and 0xFF
        val blue = color and 0xFF

        drawFilledCircleNoGL(x, y, radius, Color(red, green, blue, alpha), quality)
    }

    fun quickPolygonCircle(x: Float, y: Float, xRadius: Float, yRadius: Float, start: Int, end: Int) {
        for (i in end until start step 4) {
            val angle = MathUtils.toRadiansDouble(i)
            glVertex2d(x + sin(angle) * xRadius, y + cos(angle) * yRadius)
        }
    }

    fun drawLoadingCircle(x: Float, y: Float) {
        for (i in 0..3) {
            val rot = (System.nanoTime() / 5000000 * i % 360).toInt()
            drawCircle(x, y, 10f * i, rot - 180, rot)
        }
    }

    fun drawCircle(x: Float, y: Float, radius: Float, start: Int, end: Int) {
        drawCircle(x, y, radius, 2f, start, end, Color.WHITE)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, lineWidth: Float, start: Int, end: Int) {
        drawCircle(x, y, radius, lineWidth, start, end, Color.WHITE)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, lineWidth: Float, start: Int, end: Int, color: Color) {
        glColor(color)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor(color)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(lineWidth)
        glBegin(GL_LINE_STRIP)

        for (i in end until start step 4) {
            val angle = MathUtils.toRadiansFloat(i)
            glVertex2f(x + cos(angle) * radius * 1.001f, y + sin(angle) * radius * 1.001f)
        }

        glEnd()
        glDisable(GL_LINE_SMOOTH)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawGradientCircle(x: Float, y: Float, radius: Float, start: Int, end: Int, startColor: Color, endColor: Color) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(2f)
        glBegin(GL_LINE_STRIP)

        for (i in end until start step 4) {
            val color = ColorUtils.getGradientOffset(startColor, endColor, 1.0, (abs(System.currentTimeMillis() / 360.0 + (i * 34 / 360) * 56 / 100) / 10).toInt()).rgb
            GLUtils.color(color)

            val angle = MathUtils.toRadiansFloat(i)
            glVertex2f(x + cos(angle) * radius * 1.001f, y + sin(angle) * radius * 1.001f)

        }
        glEnd()
        glDisable(GL_LINE_SMOOTH)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawFilledCircle(x: Number, y: Number, radius: Float, color: Color) {
        drawFilledCircle(x.toFloat(), y.toFloat(), radius, color)
    }

    fun drawFilledCircle(x: Float, y: Float, radius: Float, color: Color) {
        val angleUnit = PI.toFloat() / 25 // 2 * PI / 50
        glPushAttrib(GL_ENABLE_BIT)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_TRIANGLE_FAN)
        for (i in 0 until 50) {
            GLUtils.color(color)
            glVertex2f(x + sin(i * angleUnit) * radius, y + cos(i * angleUnit) * radius)
        }
        GlStateManager.color(0f, 0f, 0f)
        glEnd()
        glPopAttrib()
    }

    /**
     * Gradient sideways
     */

    fun drawGradientSidewaysHorizontal(left: Double, top: Double, right: Double, bottom: Double, color1: Int, color2: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        quickDrawGradientSidewaysHorizontal(left, top, right, bottom, color1, color2)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
    }

    fun quickDrawGradientSidewaysHorizontal(left: Double, top: Double, right: Double, bottom: Double, color1: Int, color2: Int) {
        glBegin(GL_QUADS)
        glColor(color1)
        glVertex2d(left, top)
        glVertex2d(left, bottom)
        glColor(color2)
        glVertex2d(right, bottom)
        glVertex2d(right, top)
        glEnd()
    }

    fun drawGradientSidewaysVertical(left: Double, top: Double, right: Double, bottom: Double, color1: Int, color2: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        quickDrawGradientSidewaysVertical(left, top, right, bottom, color1, color2)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
    }

    fun quickDrawGradientSidewaysVertical(left: Double, top: Double, right: Double, bottom: Double, color1: Int, color2: Int) {
        glBegin(GL_QUADS)
        glColor(color1)
        glVertex2d(right, top)
        glVertex2d(left, top)
        glColor(color2)
        glVertex2d(left, bottom)
        glVertex2d(right, bottom)
        glEnd()
    }

    fun drawGradientSideways(left: Double, top: Double, right: Double, bottom: Double, color1: Int, color2: Int) {
        glEnable(3042)
        glDisable(3553)
        glBlendFunc(770, 771)
        glEnable(2848)
        glShadeModel(7425)
        glPushMatrix()
        glBegin(7)
        GLUtils.color(color1)
        glVertex2d(left, top)
        glVertex2d(left, bottom)
        GLUtils.color(color2)
        glVertex2d(right, bottom)
        glVertex2d(right, top)
        glEnd()
        glPopMatrix()
        glEnable(3553)
        glDisable(3042)
        glDisable(2848)
        glShadeModel(7424)
    }

}