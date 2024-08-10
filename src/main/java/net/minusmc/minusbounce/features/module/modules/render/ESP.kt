/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Render2DEvent
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.ui.font.GameFontRenderer.Companion.getColorIndex
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.render.BlendUtils
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.RenderUtils.drawEntityBox
import net.minusmc.minusbounce.utils.render.RenderUtils.draw2D
import net.minusmc.minusbounce.utils.render.RenderUtils.isInViewFrustrum
import net.minusmc.minusbounce.utils.render.WorldToScreen.getMatrix
import net.minusmc.minusbounce.utils.render.WorldToScreen.worldToScreen
import net.minusmc.minusbounce.utils.render.shader.shaders.GlowShader
import net.minusmc.minusbounce.utils.render.shader.shaders.OutlineShader
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector3f
import java.awt.Color
import java.text.DecimalFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

@ModuleInfo(name = "ESP", description = "Allows you to see targets through walls.", category = ModuleCategory.RENDER)
class ESP : Module() {
    private val decimalFormat = DecimalFormat("0.0")
    val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "WireFrame", "2D", "Real2D", "Outline", "ShaderOutline", "ShaderGlow"), "Box")
    val outlineWidth = FloatValue("Outline-Width", 3f, 0.5f, 5f) { modeValue.get().equals("outline", true) }
    val wireframeWidth = FloatValue("WireFrame-Width", 2f, 0.5f, 5f) { modeValue.get().equals("wireframe", true) }
    private val shaderOutlineRadius = FloatValue("ShaderOutline-Radius", 1.35f, 1f, 2f, "x") { modeValue.get().equals("shaderoutline", true) }
    private val shaderGlowRadius = FloatValue("ShaderGlow-Radius", 2.3f, 2f, 3f, "x") { modeValue.get().equals("shaderglow", true) }
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Health", "Rainbow", "Sky", "LiquidSlowly", "Fade"), "Custom")
    private val colorRedValue = IntegerValue("Red", 255, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 255, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)
    private val colorTeam = BoolValue("Team", false)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {

        // mode.onRender3D()


        val mode = modeValue.get()
        val mvMatrix = getMatrix(GL11.GL_MODELVIEW_MATRIX)
        val projectionMatrix = getMatrix(GL11.GL_PROJECTION_MATRIX)
        val real2d = mode.equals("real2d", true)
        if (real2d) {
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
        }
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityLivingBase && EntityUtils.isSelected(entity, true) && isInViewFrustrum(entity)) {
                val color = getColor(entity)
                
                // mode.
            }
        }
        if (real2d) {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glMatrixMode(GL11.GL_PROJECTION)
            GL11.glPopMatrix()
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPopMatrix()
            GL11.glPopAttrib()
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val shader = when (modeValue.get().lowercase()) {
            "shaderoutline" -> OutlineShader.OUTLINE_SHADER
            "shaderglow" -> GlowShader.GLOW_SHADER
            else -> return
        }

        shader.startDraw(event.partialTicks)
        renderNameTags = false

        for (entity in mc.theWorld.loadedEntityList) {
            if (!EntityUtils.isSelected(entity, false))
                continue
            
            mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
        }

        renderNameTags = true

        val radius = when (modeValue.get().lowercase()) {
            "shaderoutline" -> shaderOutlineRadius.get()
            "shaderglow" -> shaderGlowRadius.get()
            else -> 1f
        }

        shader.stopDraw(getColor(null), radius, 1f)
    }

    fun getColor(entity: Entity?): Color {
        if (entity is EntityLivingBase) {
            if (colorModeValue.get().equals("health", true))
                return BlendUtils.getHealthColor(entity.health, entity.maxHealth)

            if (entity.hurtTime > 0)
                return Color.RED

            if (colorTeam.get()) {
                val chars = entity.displayName.formattedText.toCharArray()
                var color = Int.MAX_VALUE
                for (i in chars.indices) {
                    if (chars[i] != '§' || i + 1 >= chars.size) continue
                    val index = getColorIndex(chars[i + 1])
                    if (index < 0 || index > 15) continue
                    color = ColorUtils.hexColors[index]
                    break
                }
                return Color(color)
            }
        }

        return when (colorModeValue.get()) {
            "Custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
            "Rainbow" -> Color(
                ColorUtils.getRainbowOpaque(
                    mixerSecondsValue.get(),
                    saturationValue.get(),
                    brightnessValue.get(),
                    0
                )
            )
            "LiquidSlowly" -> ColorUtils.liquidSlowly(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get())
            "Sky" -> Color(ColorUtils.skyRainbow(0, saturationValue.get(), brightnessValue.get()))
            "Fade" -> ColorUtils.fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
            else -> Color.WHITE
        }
    }

    companion object {
        @JvmField
        var renderNameTags = true
    }
}
