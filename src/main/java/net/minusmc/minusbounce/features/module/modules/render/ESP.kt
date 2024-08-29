/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.render

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.render.esps.ESPMode
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.render.BlendUtils
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.RenderUtils.isInViewFrustrum
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import java.awt.Color
import java.util.*

@ModuleInfo(name = "ESP", description = "Allows you to see targets through walls.", category = ModuleCategory.RENDER)
class ESP : Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.esps", ESPMode::class.java)
        .map { it.newInstance() as ESPMode }
        .sortedBy { it.modeName }

    private val mode: ESPMode
        get() = modes.find { modeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()

    val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "Box") {
        override fun onPreChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onPostChange(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Health", "Rainbow", "Sky", "LiquidSlowly", "Fade"), "Custom")
    private val colorRedValue = IntegerValue("Red", 255, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 255, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val mixerSecondsValue = IntegerValue("Seconds", 2, 1, 10)
    private val colorTeam = BoolValue("Team", false)

    override fun onInitialize() {
        modes.map { mode -> mode.values.forEach { value -> value.name = "${mode.modeName}-${value.name}" } }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        mode.onPreRender3D()
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityLivingBase && EntityUtils.isSelected(entity, false) && isInViewFrustrum(entity)) {
                val color = getEntityColor(entity)
                mode.onEntityRender(entity, color)
            }
        }
        mode.onPostRender3D()
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        mode.onRender2D(event, colorByMode)
    }

    @EventTarget
    fun onRenderModel(event: RenderModelEvent) {
        if (!EntityUtils.isSelected(event.entity, false))
            return

        val fancyGraphics = mc.gameSettings.fancyGraphics
        mc.gameSettings.fancyGraphics = false

        val gamma = mc.gameSettings.gammaSetting
        mc.gameSettings.gammaSetting = 100000F

        val color = getEntityColor(event.entity)

        mode.onRenderModel(event, color)

        mc.gameSettings.fancyGraphics = fancyGraphics
        mc.gameSettings.gammaSetting = gamma
    }

    @EventTarget
    fun onRenderNameTags(event: RenderNameTagsEvent) {
        mode.onRenderNameTags(event)
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

    override val values = super.values.toMutableList().also {
        modes.map {
            mode -> mode.values.forEach { value ->
                val displayableFunction = value.displayableFunction
                it.add(value.displayable { displayableFunction.invoke() && modeValue.get() == mode.modeName })
            }
        }
    }
}
