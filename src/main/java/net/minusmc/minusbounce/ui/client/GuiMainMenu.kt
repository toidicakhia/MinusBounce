/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.plugin.PluginGuiManager
import net.minusmc.minusbounce.ui.client.altmanager.GuiAltManager
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.ShaderUtils
import java.awt.Color

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    override fun initGui() {
        buttonList.add(MainButton(0, width / 2 - 55, height / 2 - 35, "Singleplayer"))
        buttonList.add(MainButton(1, width / 2 - 55, height / 2, "Multiplayer"))
        buttonList.add(MainButton(2, width / 2 - 55, height / 2 + 35, "Alt manager"))
        buttonList.add(HeaderButton(3, width - 286, 20, "Mods"))
        buttonList.add(HeaderButton(6, width - 214, 20, "Background"))
        buttonList.add(HeaderButton(4, width - 142, 20, "Options"))
        buttonList.add(HeaderButton(5, width - 70, 20, "Quit"))
        
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        RenderUtils.drawImage(ResourceLocation("minusbounce/big.png"), 15, 15, 40, 40)
        Fonts.fontSatoshiBold70.drawString("Minus", 65f, 15f, Color.WHITE.rgb, true)
        Fonts.fontSatoshiBold70.drawString("Bounce", 65f, 27f + mc.fontRendererObj.FONT_HEIGHT, Color.WHITE.rgb, true)

        Gui.drawRect(0, 0, 0, 0, Integer.MIN_VALUE)
        Fonts.fontLexend40.drawString("Version: ${MinusBounce.CLIENT_VERSION}", 3F, (height - mc.fontRendererObj.FONT_HEIGHT * 2 - 4).toFloat(), Color.WHITE.rgb, true)
        Fonts.fontLexend40.drawString("Made by ${MinusBounce.CLIENT_CREATOR}", 3F, (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), Color.WHITE.rgb, true)
        val creditInfo = "Copyright Mojang AB. Do not distribute!"
        Fonts.fontLexend40.drawString(creditInfo, width - 2f - Fonts.fontLexend40.getStringWidth(creditInfo), (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), Color.WHITE.rgb, true)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiSelectWorld(this))
            1 -> mc.displayGuiScreen(GuiMultiplayer(this))
            2 -> mc.displayGuiScreen(GuiAltManager(this))
            3 -> mc.displayGuiScreen(GuiModList(this))
            4 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            5 -> mc.shutdown()
            6 -> mc.displayGuiScreen(GuiBackground(this))
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}

class MainButton(buttonId: Int, x: Int, y: Int, buttonText: String): GuiButton(buttonId, x, y, buttonText) {
    init {
        width = 110
        height = 25
    }

    override fun drawButton(mc: Minecraft?, mouseX: Int, mouseY: Int) {
        ShaderUtils.drawRoundedRect(xPosition.toFloat(), yPosition.toFloat(), (xPosition + width).toFloat(), (yPosition + height).toFloat(), 4f, Color(249, 246, 238, 220).rgb)
        GlStateManager.resetColor()
        Fonts.fontLexend50.drawCenteredString(displayString, xPosition + width / 2f, yPosition + (height - Fonts.fontLexend40.FONT_HEIGHT) / 2f, Color(54, 69, 79).rgb, false)
    }
}

class HeaderButton(buttonId: Int, x: Int, y: Int, buttonText: String): GuiButton(buttonId, x, y, buttonText) {
    init {
        width = 70
        height = 25
    }

    override fun drawButton(mc: Minecraft?, mouseX: Int, mouseY: Int) {
        Fonts.fontLexend40.drawCenteredString(displayString, xPosition + width / 2f, yPosition + (height - Fonts.fontLexend40.FONT_HEIGHT) / 2f, Color.WHITE.rgb, false)
    }
}