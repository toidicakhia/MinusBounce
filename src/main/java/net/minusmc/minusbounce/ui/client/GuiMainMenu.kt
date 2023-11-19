/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.minusmc.minusbounce.ui.client

import net.minecraft.client.gui.*
import net.minecraftforge.fml.client.GuiModList
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.ui.client.altmanager.GuiAltManager
import net.minusmc.minusbounce.ui.font.Fonts
import java.awt.Color

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    override fun initGui() {
        val defaultHeight = (this.height / 2.5).toInt()

        this.buttonList.add(GuiButton(1, this.width / 2 - 120, defaultHeight, 100, 20, "Singleplayer"))
        this.buttonList.add(GuiButton(2, this.width / 2 + 20, defaultHeight, 100, 20, "Multiplayer"))
        this.buttonList.add(GuiButton(100, this.width / 2 - 120, defaultHeight + 24, 100, 20, "Alt manager"))
        this.buttonList.add(GuiButton(103, this.width / 2 + 20, defaultHeight + 24, 100, 20, "Mods and plugins"))
        this.buttonList.add(GuiButton(0, this.width / 2 - 120, defaultHeight + 24 * 2, 100, 20, "Options"))
        this.buttonList.add(GuiButton(4, this.width / 2 + 20, defaultHeight + 24 * 2, 100, 20, "Quit"))

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        val bHeight = (this.height / 3.5).toInt()

        Fonts.font72.drawCenteredString(MinusBounce.CLIENT_NAME, (width / 2).toFloat(), (bHeight - 20).toFloat(), Color.WHITE.rgb, false)
        Gui.drawRect(0, 0, 0, 0, Integer.MIN_VALUE)
        Fonts.font40.drawString("Version: ${MinusBounce.CLIENT_VERSION}", 3F, (height - mc.fontRendererObj.FONT_HEIGHT * 2 - 2).toFloat(), 0xffffff, false)
        Fonts.font40.drawString("Made by ${MinusBounce.CLIENT_CREATOR}", 3F, (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)
        val creditInfo = "Copyright Mojang AB. Do not distribute!"
        Fonts.font40.drawString(creditInfo, width - 3f - Fonts.font40.getStringWidth(creditInfo), (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen(GuiAltManager(this))
            103 -> mc.displayGuiScreen(GuiModList(this))
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}