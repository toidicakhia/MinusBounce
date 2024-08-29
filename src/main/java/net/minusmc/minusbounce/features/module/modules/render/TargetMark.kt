/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.utils.render.*
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.*

@ModuleInfo(name = "TargetMark", spacedName = "Target Mark", description = "Displays your KillAura's target in 3D.", category = ModuleCategory.RENDER)
class TargetMark : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Default", "Box", "Jello", "Tracers"), "Default")
    private val jelloAlphaValue = FloatValue("JelloEndAlphaPercent", 0.4f, 0f, 1f, "x") { modeValue.get().equals("jello", true) }
    private val jelloWidthValue = FloatValue("JelloCircleWidth", 3f, 0.01f, 5f) { modeValue.get().equals("jello", true) }
    private val jelloGradientHeightValue = FloatValue("JelloGradientHeight", 3f, 1f, 8f, "m") { modeValue.get().equals("jello", true) }
    private val jelloFadeSpeedValue = FloatValue("JelloFadeSpeed", 0.1f, 0.01f, 0.5f, "x") { modeValue.get().equals("jello", true) }
    private val moveMarkValue = FloatValue("MoveMarkY", 0.6f, 0f, 2f) { modeValue.get().equals("default", true) }
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Rainbow", "Sky", "Fade", "LiquidSlowly", "Health"), "Custom")
    private val colorRedValue = IntegerValue("Red", 255, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 255, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val colorAlphaValue = IntegerValue("Alpha", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val thicknessValue = FloatValue("Thickness", 1f, 0f, 5f)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)
    private val colorTeam = BoolValue("Team", false)

    private var target: EntityLivingBase? = null
    private var direction = 1
    private var currentPosY = 0.0
    private var lastPosY = 0.0

    private var progress = 0.0
    private var alphaLevel = 0f
    private var lastMS = System.currentTimeMillis()
    private var lastDeltaMS = 0L

    @EventTarget
    fun onTick(event: TickEvent) {
        if (modeValue.get().equals("jello", true))
            alphaLevel = AnimationUtils.changer(alphaLevel, if (currentTarget != null) jelloFadeSpeedValue.get() else -jelloFadeSpeedValue.get(), 0f, colorAlphaValue.get() / 255f)
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        when (modeValue.get().lowercase()) {
            "jello" -> drawJelloMode()
            "tracers" -> drawTracersMode()
            "default" -> currentTarget?.let {
                val color = ColorUtils.reAlpha(getEntityColor(it), colorAlphaValue.get())
                RenderUtils.drawPlatform(it, color, moveMarkValue.get())
            }
            else -> currentTarget?.let {
                val color = ColorUtils.reAlpha(getEntityColor(it), colorAlphaValue.get())
                RenderUtils.drawEntityBox(it, color, false)
            }
        }
    }

    private fun drawJelloMode() {
        lastPosY = currentPosY

        val currentTime = System.currentTimeMillis()

        if (alphaLevel > 0f) {
            if (currentTime - lastMS >= 1000L) {
                direction = -direction
                lastMS = currentTime
            }
            progress = if (direction > 0) (currentTime - lastMS) / 1000.0 else (1000L - currentTime + lastMS) / 1000.0
            lastDeltaMS = currentTime - lastMS
        } else lastMS = currentTime - lastDeltaMS

        if (target != currentTarget)
            target = currentTarget

        val target = this.target ?: return
        val boundingBox = target.entityBoundingBox

        val radius = boundingBox.maxX - boundingBox.minX
        val height = boundingBox.maxY - boundingBox.minY

        val posX = MathUtils.interpolate(target.posX, target.lastTickPosX, mc.timer.renderPartialTicks)
        val posY = MathUtils.interpolate(target.posY, target.lastTickPosY, mc.timer.renderPartialTicks)
        val posZ = MathUtils.interpolate(target.posZ, target.lastTickPosZ, mc.timer.renderPartialTicks)

        currentPosY = EaseUtils.easeInOutQuart(progress) * height
        val deltaY = (if (direction < 0) currentPosY - lastPosY else lastPosY - currentPosY) * jelloGradientHeightValue.get()
        
        if (alphaLevel <= 0) {
            this.target = null
            return
        }

        val color = getEntityColor(target)
        val reAlphaColor = ColorUtils.reAlpha(color, alphaLevel)
        
        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDepthMask(false)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
        GL11.glDisable(2884)
        GL11.glTranslated(-mc.renderManager.viewerPosX, -mc.renderManager.viewerPosY, -mc.renderManager.viewerPosZ)
        GL11.glBegin(GL11.GL_QUAD_STRIP)
        
        for (i in 0..360) {
            val calc = MathUtils.toRadiansDouble(i)
            val posX2 = posX - sin(calc) * radius
            val posZ2 = posZ + cos(calc) * radius
            GLUtils.glColor(reAlphaColor, 0f)
            GL11.glVertex3d(posX2, posY + currentPosY + deltaY, posZ2)
            GLUtils.glColor(reAlphaColor, alphaLevel * jelloAlphaValue.get())
            GL11.glVertex3d(posX2, posY + currentPosY, posZ2)
        }

        GL11.glEnd()

        GL11.glLineWidth(jelloWidthValue.get())
        GL11.glBegin(GL11.GL_LINE_LOOP)
        GLUtils.glColor(reAlphaColor)

        for (i in 0..360) {
            val angle = MathUtils.toRadiansDouble(i)

            val x2 = posX - sin(angle) * radius
            val z2 = posZ + cos(angle) * radius

            GL11.glVertex3d(x2, posY + currentPosY, z2)
        }

        GL11.glEnd()

        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f)
    }

    private fun drawTracersMode() {
        val target = currentTarget ?: return

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(thicknessValue.get())
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        GL11.glBegin(GL11.GL_LINES)

        RenderUtils.drawTraces(target, getEntityColor(target), false)
        
        GL11.glEnd()

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GlStateManager.resetColor()
    }

    val colorByMode: Color
        get() = when (colorModeValue.get().lowercase()) {
            "rainbow" -> ColorUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0)
            "liquidslowly" -> ColorUtils.getLiquidSlowlyColor(0, saturationValue.get(), brightnessValue.get())
            "sky" -> ColorUtils.getSkyRainbowColor(0, saturationValue.get(), brightnessValue.get())
            "fade" -> ColorUtils.getFadeColor(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
            else -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
        }

    private fun getEntityColor(entity: Entity?) = ColorUtils.getEntityColor(entity, colorTeam.get(), colorModeValue.get().equals("health", true), !modeValue.get().equals("jello", true)) ?: colorByMode

    fun canPushPopMatrix() = modeValue.get().equals("tracers", true) && target != null

    private val killAura: KillAura
        get() = MinusBounce.moduleManager[KillAura::class.java]!!

    private val currentTarget: EntityLivingBase?
        get() = killAura.target

    override val tag: String
        get() = modeValue.get()
}