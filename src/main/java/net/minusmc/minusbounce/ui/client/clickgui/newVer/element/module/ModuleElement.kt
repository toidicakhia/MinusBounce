/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui.newVer.element.module

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.ui.client.clickgui.newVer.ColorManager
import net.minusmc.minusbounce.ui.client.clickgui.newVer.element.components.ToggleSwitch
import net.minusmc.minusbounce.ui.client.clickgui.newVer.element.module.value.ValueElement
import net.minusmc.minusbounce.ui.client.clickgui.newVer.element.module.value.impl.BooleanElement
import net.minusmc.minusbounce.ui.client.clickgui.newVer.element.module.value.impl.FloatElement
import net.minusmc.minusbounce.ui.client.clickgui.newVer.element.module.value.impl.IntElement
import net.minusmc.minusbounce.ui.client.clickgui.newVer.element.module.value.impl.ListElement
import net.minusmc.minusbounce.ui.client.clickgui.newVer.extensions.animSmooth
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.utils.MouseUtils
import net.minusmc.minusbounce.utils.render.BlendUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.Stencil
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.*
import java.awt.Color

open class ModuleElement(val module: Module): MinecraftInstance() {

    companion object {
        protected val expandIcon = ResourceLocation("minusbounce/expand.png") }

    private val toggleSwitch = ToggleSwitch()
    private val valueElements = mutableListOf<ValueElement<*>>()

    var animHeight = 0F
    private var fadeKeybind = 0F
    private var animPercent = 0F

    private var listeningToKey = false
    private var expanded = false

    init {
        for (value in module.values) {
            if (value is BoolValue)
                valueElements.add(BooleanElement(value))
            if (value is ListValue)
                valueElements.add(ListElement(value))
            if (value is IntegerValue)
                valueElements.add(IntElement(value))
            if (value is FloatValue)
                valueElements.add(FloatElement(value))
        }
    }

    fun drawElement(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, height: Float, accentColor: Color): Float {
        animPercent = animPercent.animSmooth(if (expanded) 100F else 0F, 0.5F)
        var expectedHeight = 0F
        for (ve in valueElements)
            if (ve.isDisplayable())
                expectedHeight += ve.valueHeight
        animHeight = animPercent / 100F * (expectedHeight + 10F)

        RenderUtils.originalRoundedRect(x + 9.5F, y + 4.5F, x + width - 9.5F, y + height + animHeight - 4.5F, 4F, ColorManager.buttonOutline.rgb)
        Stencil.write(true)
        RenderUtils.originalRoundedRect(x + 10F, y + 5F, x + width - 10F, y + height + animHeight - 5F, 4F, ColorManager.moduleBackground.rgb)
        Stencil.erase(true)
        RenderUtils.newDrawRect(x + 10F, y + height - 5F, x + width - 10F, y + height - 4.5F, 4281348144L.toInt())
        Fonts.font40.drawString(module.name, x + 20F, y + height / 2F - Fonts.font40.FONT_HEIGHT + 3F, -1)
        Fonts.fontTiny.drawString(module.description, x + 20F, y + height / 2F + 4F, 10526880L.toInt())

        val keyName = if (listeningToKey) "Listening" else Keyboard.getKeyName(module.keyBind)

        fadeKeybind = if (MouseUtils.mouseWithinBounds(mouseX, mouseY,
                x + 25F + Fonts.font40.getStringWidth(module.name),
                y + height / 2F - Fonts.font40.FONT_HEIGHT + 2F,
                x + 35F + Fonts.font40.getStringWidth(module.name) + Fonts.fontTiny.getStringWidth(keyName),
                y + height / 2F))
            (fadeKeybind + 0.1F * RenderUtils.deltaTime * 0.025F).coerceIn(0F, 1F)
        else
            (fadeKeybind - 0.1F * RenderUtils.deltaTime * 0.025F).coerceIn(0F, 1F)

        RenderUtils.originalRoundedRect(
                x + 25F + Fonts.font40.getStringWidth(module.name),
                y + height / 2F - Fonts.font40.FONT_HEIGHT + 2F,
                x + 35F + Fonts.font40.getStringWidth(module.name) + Fonts.fontTiny.getStringWidth(keyName),
                y + height / 2F, 2F, BlendUtils.blend(Color(4282729797L.toInt()), Color(4281677109L.toInt()), fadeKeybind.toDouble())!!.rgb)
        Fonts.fontTiny.drawString(keyName, x + 30.5F + Fonts.font40.getStringWidth(module.name), y + height / 2F - Fonts.font40.FONT_HEIGHT + 5.5F, -1)

        toggleSwitch.state = module.state

        if (module.values.isNotEmpty()) {
            RenderUtils.newDrawRect(x + width - 40F, y + 5F, x + width - 39.5F, y + height - 5F, 4281348144L.toInt())
            GlStateManager.resetColor()
            glPushMatrix()
            glTranslatef(x + width - 25F, y + height / 2F, 0F)
            glPushMatrix()
            glRotatef(180F * (animHeight / (expectedHeight + 10F)), 0F, 0F, 1F)
            glColor4f(1F, 1F, 1F, 1F)
            RenderUtils.drawImage(expandIcon, -4, -4, 8, 8)
            glPopMatrix()
            glPopMatrix()
            toggleSwitch.onDraw(x + width - 70F, y + height / 2F - 5F, 20F, 10F, Color(4280624421L.toInt()), accentColor)
        } else
            toggleSwitch.onDraw(x + width - 40F, y + height / 2F - 5F, 20F, 10F, Color(4280624421L.toInt()), accentColor)

        if (expanded || animHeight > 0F) {
            var startYPos = y + height
            for (ve in valueElements)
                if (ve.isDisplayable())
                    startYPos += ve.drawElement(mouseX, mouseY, x + 10F, startYPos, width - 20F, Color(4280624421L.toInt()), accentColor)
        }
        Stencil.dispose()

        return height + animHeight
    }

    fun handleClick(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, height: Float) {
        if (listeningToKey) {
            resetState()
            return
        }
        val keyName = if (listeningToKey) "Listening" else Keyboard.getKeyName(module.keyBind)
        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, 
                x + 25F + Fonts.font40.getStringWidth(module.name),
                y + height / 2F - Fonts.font40.FONT_HEIGHT + 2F,
                x + 35F + Fonts.font40.getStringWidth(module.name) + Fonts.fontTiny.getStringWidth(keyName),
                y + height / 2F)) {
            listeningToKey = true
            return
        }
        if (MouseUtils.mouseWithinBounds(mouseX, mouseY, 
                x + width - if (module.values.isNotEmpty()) 70F else 40F, y,
                x + width - if (module.values.isNotEmpty()) 50F else 20F, y + height))
            module.toggle()
        if (module.values.isNotEmpty() && MouseUtils.mouseWithinBounds(mouseX, mouseY, x + width - 40F, y, x + width - 10F, y + height))
            expanded = !expanded
        if (expanded) {
            var startY = y + height
            for (ve in valueElements) {
                if (!ve.isDisplayable()) continue
                ve.onClick(mouseX, mouseY, x + 10F, startY, width - 20F)
                startY += ve.valueHeight
            }
        }
    }

    fun handleRelease(mouseX: Int, mouseY: Int, x: Float, y: Float, width: Float, height: Float) {
        if (expanded) {
            var startY = y + height
            for (ve in valueElements) {
                if (!ve.isDisplayable()) continue
                ve.onRelease(mouseX, mouseY, x + 10F, startY, width - 20F)
                startY += ve.valueHeight
            }
        }
    }

    fun handleKeyTyped(typed: Char, code: Int): Boolean {
        if (listeningToKey) {
            if (code == 1) {
                module.keyBind = 0
                listeningToKey = false
            } else {
                module.keyBind = code
                listeningToKey = false
            }
            return true
        }
        if (expanded)
            for (ve in valueElements)
                if (ve.isDisplayable() && ve.onKeyPress(typed, code)) return true
        return false
    }

    fun listeningKeybind(): Boolean = listeningToKey
    fun resetState() {
        listeningToKey = false
    }

}
