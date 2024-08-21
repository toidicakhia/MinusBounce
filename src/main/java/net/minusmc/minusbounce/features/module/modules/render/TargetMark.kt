/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.render

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.AxisAlignedBB
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.event.TickEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.combat.KillAura
import net.minusmc.minusbounce.utils.render.*
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
    private val modeValue = ListValue("Mode", arrayOf("Default", "Box", "Jello"), "Default")
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
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)
    private val colorTeam = BoolValue("Team", false)


    private var entity: EntityLivingBase? = null
    private var direction = 1
    private var currentPosY = 0.0
    private var lastPosY = 0.0

    private var progress = 0.0
    private var alphaLevel = 0f
    private var lastMS = System.currentTimeMillis()
    private var lastDeltaMS = 0L

    @EventTarget
    fun onTick(event: TickEvent) {
        if (modeValue.get().equals("jello", true) && !killAura.targetModeValue.get().equals("multi", true))
            alphaLevel = AnimationUtils.changer(alphaLevel, if (currentTarget != null) jelloFadeSpeedValue.get() else -jelloFadeSpeedValue.get(), 0f, colorAlphaValue.get() / 255f)
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (killAura.targetModeValue.get().equals("multi", true))
            return

        when (modeValue.get().lowercase()) {
            "jello" -> drawJelloMode()
            "default" -> currentTarget?.let {
                val color = if (killAura.hitable) getEntityColor(it) else Color(255, 0, 0)
                val reAlphaColor = ColorUtils.reAlpha(color, colorAlphaValue.get())

                RenderUtils.drawPlatform(it, reAlphaColor, moveMarkValue.get())
            }
            else -> currentTarget?.let {
                val color = if (killAura.hitable) getEntityColor(it) else Color(255, 0, 0)
                val reAlphaColor = ColorUtils.reAlpha(color, colorAlphaValue.get())

                RenderUtils.drawEntityBox(it, reAlphaColor, false)
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

        if (currentTarget != null)
            entity = currentTarget

        val target = this.entity ?: return
        val boundingBox = target.entityBoundingBox

        val radius = boundingBox.maxX - boundingBox.minX
        val height = boundingBox.maxY - boundingBox.minY

        val posX = MathUtils.interpolate(target.posX, target.lastTickPosX, mc.timer.renderPartialTicks)
        val posY = MathUtils.interpolate(target.posY, target.lastTickPosY, mc.timer.renderPartialTicks)
        val posZ = MathUtils.interpolate(target.posZ, target.lastTickPosZ, mc.timer.renderPartialTicks)

        currentPosY = EaseUtils.easeInOutQuart(progress) * height
        val deltaY = (if (direction > 0) currentPosY - lastPosY else lastPosY - currentPosY) * jelloGradientHeightValue.get()
        
        if (alphaLevel <= 0 && entity != null) {
            entity = null
            return
        }

        val color = getEntityColor(entity)
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
            GLUtils.glColor(color, 0f)
            GL11.glVertex3d(posX2, currentPosY + currentPosY + deltaY, posZ2)
            GLUtils.glColor(color, alphaLevel * jelloAlphaValue.get())
            GL11.glVertex3d(posX2, currentPosY + currentPosY, posZ2)
        }

        GL11.glEnd()
        RenderUtils.draw3DCircle(posX, currentPosY + currentPosY, posZ, jelloWidthValue.get(), radius, color)
        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f)
    }

    val colorByMode: Color
        get() = when (colorModeValue.get().lowercase()) {
            "rainbow" -> ColorUtils.getRainbowOpaque(mixerSecondsValue.get(), saturationValue.get(), brightnessValue.get(), 0)
            "liquidslowly" -> ColorUtils.getLiquidSlowlyColor(0, saturationValue.get(), brightnessValue.get())
            "sky" -> ColorUtils.getSkyRainbowColor(0, saturationValue.get(), brightnessValue.get())
            "fade" -> ColorUtils.getFadeColor(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get()), 0, 100)
            else -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
        }

    fun getEntityColor(entity: Entity?) = ColorUtils.getEntityColor(entity, colorTeam.get(), colorModeValue.get().equals("health", true), true) ?: colorByMode

    override val tag: String
        get() = modeValue.get()

    private val killAura: KillAura
        get() = MinusBounce.moduleManager[KillAura::class.java]!!

    private val currentTarget: EntityLivingBase?
        get() = killAura.target
}