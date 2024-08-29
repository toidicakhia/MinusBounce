package net.minusmc.minusbounce.features.module.modules.render

import net.minecraft.entity.EntityLivingBase
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "Tracers", description = "Draws a line to targets around you.", category = ModuleCategory.RENDER)
class Tracers : Module() {
    private val colorMode = ListValue("Color", arrayOf("Custom", "DistanceColor", "Rainbow"), "Custom")
    private val thicknessValue = FloatValue("Thickness", 2F, 1F, 5F)
    private val colorRedValue = IntegerValue("Red", 0, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 160, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)

    private val directLineValue = BoolValue("DirectLine", false)
    private val fovModeValue = ListValue("FOVMode", arrayOf("All", "Back", "Front"), "All")
    private val fovValue = FloatValue("FOV", 180F, 0F, 180F) { !fovModeValue.get().equals("all", true) }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(thicknessValue.get())
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        GL11.glBegin(GL11.GL_LINES)

        for (entity in mc.theWorld.loadedEntityList) {
            val canDrawTracer = when (fovModeValue.get().lowercase()) {
                "back" -> RotationUtils.getRotationBackDifference(entity) <= fovValue.get()
                "front" -> RotationUtils.getRotationDifference(entity) <= fovValue.get()
                else -> true
            }

            if (canDrawTracer && EntityUtils.isSelected(entity, false) && entity != mc.thePlayer) {
                var distance = (mc.thePlayer.getDistanceToEntity(entity) * 2).toInt().coerceAtMost(255)

                val color = when {
                    entity is EntityLivingBase && EntityUtils.isFriend(entity) -> Color(0, 0, 255, 150)
                    colorMode.get().equals("custom", true) -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), 150)
                    colorMode.get().equals("distancecolor", true) -> Color(255 - distance, distance, 0, 150)
                    colorMode.get().equals("rainbow", true) -> ColorUtils.rainbow()
                    else -> Color(255, 255, 255, 150)
                }

                RenderUtils.drawTraces(entity, color, !directLineValue.get())
            }

        }

        GL11.glEnd()

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GlStateManager.resetColor()
    }
}