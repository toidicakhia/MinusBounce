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
import net.minusmc.minusbounce.utils.render.*
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.animation.EaseInOutTimer
import net.minusmc.minusbounce.utils.geometry.Rectagle
import java.awt.Color
import org.lwjgl.opengl.GL11.*

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    private val logoAnimation = EaseInOutTimer()

    override fun initGui() {
        buttonList.add(MainButton(0, width / 2 - 120, height / 2 - 25, "Singleplayer"))
        buttonList.add(MainButton(1, width / 2 + 10, height / 2 - 25, "Multiplayer"))
        buttonList.add(MainButton(2, width / 2 - 120, height / 2 + 10, "Alt manager"))
        buttonList.add(MainButton(3, width / 2 + 10, height / 2 + 10, "Settings"))
        buttonList.add(MainButton(4, width / 2 - 120, height / 2 + 45, "Client settings"))
        buttonList.add(MainButton(5, width / 2 + 10, height / 2 + 45, "Quit"))

        super.initGui()
    }
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        logoAnimation.update()
        val easeProgress = EaseUtils.easeOutBack(logoAnimation.progress.toDouble())
        val deltaX = easeProgress * 45
        val deltaXText = easeProgress * 60
        val patternLogoBox = Rectagle(width / 2 - 78, height / 2 - 110, width / 2 - 18, height / 2 - 30)

        drawLogoText("Minus", width / 2 - 65f + deltaXText.toFloat(), height / 2 - 100f, patternLogoBox)
        drawLogoText("Bounce", width / 2 - 65f + deltaXText.toFloat(), height / 2 - 80f + mc.fontRendererObj.FONT_HEIGHT, patternLogoBox)
        RenderUtils.drawImage(ResourceLocation("minusbounce/big.png"), width / 2 - 28 - deltaX.toInt(), height / 2 - 100, 56, 56)
        
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
            3 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            4 -> mc.displayGuiScreen(GuiBackground(this))
            5 -> mc.shutdown()
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}

    private fun drawLogoText(text: String, x: Float, y: Float, box: Rectagle) {
        var currentX = x
        val currentY = y
        for (char in text) {
            val charWidth = Fonts.fontSatoshiBold95.getCharWidth(char)

            if (!box.isMouseHover(currentX, currentY))
                Fonts.fontSatoshiBold95.drawString(char.toString(), currentX, currentY, Color.WHITE.rgb, true)
            
            currentX += charWidth
        }
    }
}

class MainButton(buttonId: Int, x: Int, y: Int, buttonText: String): GuiButton(buttonId, x, y, buttonText) {
    init {
        width = 110
        height = 25
    }

    override fun drawButton(mc: Minecraft?, mouseX: Int, mouseY: Int) {
        val currentHover = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height
        
        if (currentHover)
            RenderUtils.drawRoundedGradientRectCorner(xPosition, yPosition, xPosition + width, yPosition + height, 4f, Color(85, 121, 175, 180).rgb, Color(0, 0, 58, 180).rgb)
        else
            RenderUtils.drawRoundedGradientRectCorner(xPosition, yPosition, xPosition + width, yPosition + height, 4f, Color(0, 0, 58, 180).rgb, Color(85, 121, 175, 180).rgb)

        GlStateManager.resetColor()
        Fonts.fontLexend50.drawCenteredString(displayString, xPosition + width / 2f, yPosition + (height - Fonts.font50.FONT_HEIGHT) / 2f + 2, Color.WHITE.rgb, false)
    }
}