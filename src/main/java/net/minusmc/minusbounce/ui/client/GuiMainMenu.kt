/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.Render
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
        buttonList.add(MainButton(0, 20, 80, "Singleplayer", ResourceLocation("minusbounce/menu/singleplayer.png")))
        buttonList.add(MainButton(1, 20, 140, "Multiplayer", ResourceLocation("minusbounce/menu/multiplayer.png")))
        buttonList.add(MainButton(3, 20, 200, "Settings", ResourceLocation("minusbounce/menu/settings.png")))

        super.initGui()
    }
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        logoAnimation.update()
        val easeProgress = EaseUtils.easeOutBack(logoAnimation.progress.toDouble())
        val deltaX = easeProgress * 36
        val deltaXText = easeProgress * 72
        val patternLogoBox = Rectagle(0, 0, 90, 50)

        drawLogoText("Minus", 25f + deltaXText.toFloat(), 15f, patternLogoBox)
        drawLogoText("Bounce", 25f + deltaXText.toFloat(), 30f + mc.fontRendererObj.FONT_HEIGHT, patternLogoBox)
        RenderUtils.drawImage(ResourceLocation("minusbounce/big.png"), 77 - deltaX.toInt(), 15, 45, 45)
        PƠ
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

    private fun drawLogoText(text: String, x: Float, y: Float, box: Rectagle) {
        var currentX = x
        val currentY = y
        for (char in text) {
            val charWidth = Fonts.fontSatoshiBold80.getCharWidth(char)

            if (!box.isMouseHover(currentX, currentY))
                Fonts.fontSatoshiBold80.drawString(char.toString(), currentX, currentY, Color.WHITE.rgb, true)
            
            currentX += charWidth
        }
    }
}

class MainButton(buttonId: Int, x: Int, y: Int, buttonText: String, val width: Int, val height: Int, private val image: ResourceLocation): GuiButton(buttonId, x, y, buttonText) {
    constructor(buttonId: Int, x: Int, y: Int, buttonText: String, image: ResourceLocation): this(buttonId, x, y, buttonText, 180, 50, image)

    override fun drawButton(mc: Minecraft?, mouseX: Int, mouseY: Int) {
        val currentHover = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height

        RenderUtils.drawRoundedRect(xPosition, yPosition, xPosition + width, yPosition + height, 4f, Color(29, 78, 216).rgb)
        RenderUtils.drawImage(image,xPosition + 10, yPosition + 10, height - 20, height - 20)

        GlStateManager.resetColor()
        Fonts.fontLexend50.drawString(displayString, xPosition + height.toFloat(), yPosition + (height - Fonts.font50.FONT_HEIGHT) / 2f + 2, Color.WHITE.rgb, false)
    }
}