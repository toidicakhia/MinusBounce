/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render.shader

import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.awt.Color

/**
 * @author TheSlowly
 */
abstract class FramebufferShader(fragmentShader: String) : Shader(fragmentShader) {
    protected var red = 0f
    protected var green = 0f
    protected var blue = 0f
    protected var alpha = 1f
    protected var radius = 2f
    protected var quality = 1f
    private var entityShadows = false

    companion object {
        private var framebuffer: Framebuffer? = null
    }

    fun startDraw(partialTicks: Float) {
        GlStateManager.enableAlpha()
        GlStateManager.pushMatrix()
        GlStateManager.pushAttrib()

        framebuffer = setupFrameBuffer(framebuffer)
        framebuffer?.let {
            it.framebufferClear()
            it.bindFramebuffer(true)
        }

        entityShadows = mc.gameSettings.entityShadows
        mc.gameSettings.entityShadows = false
        mc.entityRenderer.setupCameraTransform(partialTicks, 0)
    }

    fun stopDraw(color: Color, radius: Float, quality: Float) {
        mc.gameSettings.entityShadows = entityShadows
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        mc.framebuffer.bindFramebuffer(true)
        red = color.red / 255f
        green = color.green / 255f
        blue = color.blue / 255f
        alpha = color.alpha / 255f
        this.radius = radius
        this.quality = quality
        mc.entityRenderer.disableLightmap()
        RenderHelper.disableStandardItemLighting()
        startShader()
        mc.entityRenderer.setupOverlayRendering()
        drawFramebuffer(Companion.framebuffer)
        stopShader()
        mc.entityRenderer.disableLightmap()
        GlStateManager.popMatrix()
        GlStateManager.popAttrib()
    }

    /**
     * @param frameBuffer
     * @return frameBuffer
     * @author TheSlowly
     */
    fun setupFrameBuffer(frameBuffer: Framebuffer?): Framebuffer {
        frameBuffer?.deleteFramebuffer()
        return Framebuffer(mc.displayWidth, mc.displayHeight, true)
    }

    /**
     * @author TheSlowly
     */
    fun drawFramebuffer(framebuffer: Framebuffer?) {
        framebuffer ?: return

        val scaledResolution = ScaledResolution(mc)
        val scaledWidth = scaledResolution.scaledWidth.toDouble()
        val scaledHeight = scaledResolution.scaledHeight.toDouble()

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture)
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2d(0.0, 1.0)
        GL11.glVertex2d(0.0, 0.0)
        GL11.glTexCoord2d(0.0, 0.0)
        GL11.glVertex2d(0.0, scaledHeight)
        GL11.glTexCoord2d(1.0, 0.0)
        GL11.glVertex2d(scaledWidth, scaledHeight)
        GL11.glTexCoord2d(1.0, 1.0)
        GL11.glVertex2d(scaledWidth, 0.0)
        GL11.glEnd()
        GL20.glUseProgram(0)
    }
}
