/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.hud.element.elements

import net.minecraft.block.material.Material
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.client.hud.element.Element
import net.minusmc.minusbounce.ui.client.hud.element.ElementInfo
import net.minusmc.minusbounce.ui.client.hud.element.Side
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.ListValue

/**
 * CustomHUD Armor element
 *
 * Shows a horizontal display of current armor
 */
@ElementInfo(name = "Armor")
class Armor(x: Double = -8.0, y: Double = 57.0, scale: Float = 1F, side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    private val modeValue = ListValue("Mode", arrayOf("LiquidBounce", "Exhibition"), "LiquidBounce")
    private val alignment = ListValue("Alignment", arrayOf("Horizontal", "Vertical"), "Horizontal")

    /**
     * Draw element
     */
    override fun drawElement(): Border {
        if (mc.playerController.isNotCreative) {
            val renderItem = mc.renderItem
            val isInsideWater = mc.thePlayer.isInsideOfMaterial(Material.water)

            var x = 1
            var y = if (isInsideWater) -10 else 0

            RenderHelper.enableGUIStandardItemLighting()
            
            for (index in 3 downTo 0) {
                val stack = mc.thePlayer.inventory.armorInventory[index] ?: continue

                renderItem.renderItemIntoGUI(stack, x, y)
                renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y)
                when (modeValue.get().lowercase()) {
                    "exhibition" -> {
                        RenderUtils.drawExhiEnchants(stack, x, y)
                        increasePos(16)
                    }
                    else -> increasePos(18)
                }
            }

            if (modeValue.equals("Exhibition")) {
                val mainStack = mc.thePlayer.heldItem
                if (mainStack != null && mainStack.item != null) {
                    renderItem.renderItemIntoGUI(mainStack, x, y)
                    renderItem.renderItemOverlays(mc.fontRendererObj, mainStack, x, y)
                    RenderUtils.drawExhiEnchants(mainStack, x, y)
                }
            }
            
            RenderHelper.disableStandardItemLighting()
            GlStateManager.enableAlpha()
            GlStateManager.disableBlend()
            GlStateManager.disableLighting()
            GlStateManager.disableCull()
        }

        return when (modeValue.get().lowercase()) {
            "exhibition" -> if (alignment.equals("Horizontal")) Border(0F, 0F, 80F, 17F) else Border(0F, 0F, 18F, 80F)
            else -> if (alignment.equals("Horizontal")) Border(0F, 0F, 72F, 17F) else Border(0F, 0F, 18F, 72F)
        }      
    }

    fun increasePos(inc: Int) {
        when (alignment.get().lowercase()) {
            "horizontal" -> x += inc
            "vertical" -> y += inc
        }
    }
}
