/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui.style

import net.minusmc.minusbounce.ui.client.clickgui.Panel
import net.minusmc.minusbounce.ui.client.clickgui.elements.ButtonElement
import net.minusmc.minusbounce.ui.client.clickgui.elements.ModuleElement
import net.minusmc.minusbounce.utils.MinecraftInstance
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.max

abstract class Style : MinecraftInstance() {
    protected var mouseDown = false
    protected var rightMouseDown = false
    abstract fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel?)
    abstract fun drawDescription(mouseX: Int, mouseY: Int, text: String?)
    abstract fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement?)
    abstract fun drawModuleElement(mouseX: Int, mouseY: Int, moduleElement: ModuleElement?)

    fun round(f: Float): BigDecimal {
        var bd = BigDecimal(f.toString())
        bd = bd.setScale(2, 4)
        return bd
    }

    fun hoverColor(color: Color?, hover: Int): Color {
        val r = color!!.red - (hover * 2)
        val g = color.green - (hover * 2)
        val b = color.blue - (hover * 2)
        return Color(max(r.toDouble(), 0.0).toInt(), max(g.toDouble(), 0.0).toInt(), max(b.toDouble(), 0.0).toInt(), color.alpha)
    }
}
