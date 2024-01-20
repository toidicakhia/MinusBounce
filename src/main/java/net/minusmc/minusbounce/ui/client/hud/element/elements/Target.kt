/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.hud.element.elements

import net.minecraft.client.gui.GuiChat
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.client.hud.designer.GuiHudDesigner
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.client.hud.element.Element
import net.minusmc.minusbounce.ui.client.hud.element.ElementInfo
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import net.minusmc.minusbounce.utils.render.*
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * A target hud
 */
@ElementInfo(name = "Target", disableScale = true, retrieveDamage = true)
class Target : Element() {
    override fun drawElement(): Border? {
        target ?: return null
        val playerInfo = mc.netHandler.getPlayerInfo(target!!.uniqueID) ?: return null

        RenderUtils.drawRoundedCornerRect(0f, 0f, 150f, 50f, 6f, Color(0, 0, 0, 30).rgb)
        Fonts.fontTenacity40.drawString("Target: ${target!!.name}", 50f, 8f, Color(255, 255, 255, 255).rgb)
        RenderUtils.drawHead(playerInfo.locationSkin, 10, 10, 30, 30)

        return Border(0f, 0f, 150f, 50f)
    }

    val target: EntityPlayer?
        get() {
            val kaTarget = MinusBounce.combatManager.target
            return if (kaTarget != null && kaTarget is EntityPlayer && mc.thePlayer.getDistanceToEntityBox(kaTarget) <= 8.0) kaTarget 
                else if (mc.currentScreen is GuiChat || mc.currentScreen is GuiHudDesigner) mc.thePlayer 
                else null
        }
}
