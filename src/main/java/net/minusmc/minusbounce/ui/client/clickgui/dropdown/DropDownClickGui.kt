package net.minusmc.minusbounce.ui.client.clickgui.dropdown

import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI
import net.minusmc.minusbounce.features.module.modules.client.ClickGUI.accentColor
import net.minusmc.minusbounce.ui.client.clickgui.dropdown.elements.ButtonElement
import net.minusmc.minusbounce.ui.client.clickgui.dropdown.elements.ModuleElement
import net.minusmc.minusbounce.ui.font.*
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.EaseUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minusmc.minusbounce.utils.misc.MathUtils.round
import net.minecraft.util.*
import net.minecraft.client.gui.GuiScreen
import net.minusmc.minusbounce.value.*
import org.lwjgl.input.Mouse
import java.io.IOException
import java.util.*
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class DropDownClickGui: GuiScreen() {
    val panels = mutableListOf<Panel>()
    private var mouseX = 0
    private var mouseY = 0
    var slide = 0.0
    var progress = 0.0
    var lastMS = System.currentTimeMillis()

	var mouseDown = false
    var rightMouseDown = false
    val guiColor: Int
        get() = ClickGUI.accentColor!!.rgb

    var yPos = 0

    init {
        val width = 100
        val height = 18
        var yPos = 5
        for (category in ModuleCategory.values()) {
            panels.add(object : Panel(category.displayName, 100, yPos, width, height, false) {
                override fun setupItems() = MinusBounce.moduleManager.modules
                    .filter {it.category == category}
                    .forEach {elements.add(ModuleElement(it))}
            })
            yPos += 20
        }
        yPos += 20
    }

    override fun initGui() {
        progress = 0.0
        slide = progress
        lastMS = System.currentTimeMillis()
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val clickGuiModule = MinusBounce.moduleManager[ClickGUI::class.java]!!

        var mouseX = mouseX
        var mouseY = mouseY
        progress = if (progress < 1) 
            ((System.currentTimeMillis() - lastMS).toFloat() / (500f / ClickGUI.animSpeedValue.get())).toDouble() else 1.0
        
        slide = when (clickGuiModule.animationValue.get().lowercase()) {
            "zoombounce" -> EaseUtils.easeOutBack(progress)
            "slide", "zoom" -> EaseUtils.easeOutQuart(progress)
            else -> 1.0
        }

        // Enable DisplayList optimization
        AWTFontRenderer.assumeNonVolatile = true
        val scale = clickGuiModule.scaleValue.get().toDouble()
        mouseX = (mouseX / scale).toInt()
        mouseY = (mouseY / scale).toInt()
        this.mouseX = mouseX
        this.mouseY = mouseY

        when (clickGuiModule.backgroundValue.get().lowercase()) {
            "default" -> drawDefaultBackground()
            "gradient" -> drawGradientRect(0, 0, width, height, ColorUtils.reAlpha(ClickGUI.accentColor!!, clickGuiModule.gradEndValue.get()).rgb, ColorUtils.reAlpha(ClickGUI.accentColor!!, clickGuiModule.gradStartValue.get()).rgb)
        }

        GlStateManager.disableAlpha()
        GlStateManager.enableAlpha()

        when (clickGuiModule.animationValue.get().lowercase()) {
            "slide" -> {
                GlStateManager.translate(0.0, (1.0 - slide) * height * 2.0, 0.0)
                GlStateManager.scale(scale, scale, scale)
            }
            "zoom" -> {
                GlStateManager.translate((1.0 - slide) * (width / 2.0), (1.0 - slide) * (height / 2.0), (1.0 - slide) * (width / 2.0))
                GlStateManager.scale(scale * slide, scale * slide, scale * slide)
            }
            "zoombounce" -> {
                GlStateManager.translate((1.0 - slide) * (width / 2.0), (1.0 - slide) * (height / 2.0), 0.0)
                GlStateManager.scale(scale * slide, scale * slide, scale * slide)
            }
            "none" -> GlStateManager.scale(scale, scale, scale)
        }

        for (panel in panels) {
            panel.updateFade(RenderUtils.deltaTime)
            panel.drawScreen(mouseX, mouseY, partialTicks)
        }

        for (panel in panels) {
            for (element in panel.elements) {
                if (element is ModuleElement) {
                    if (mouseX != 0 && mouseY != 0 && element.isHovering(mouseX, mouseY) && element.isVisible && element.y <= panel.y + panel.getFade()) 
                        drawDescription(mouseX, mouseY, element.module.description)
                }
            }
        }

        GlStateManager.disableLighting()
        RenderHelper.disableStandardItemLighting()

        when (clickGuiModule.animationValue.get().lowercase()) {
            "slide" -> GlStateManager.translate(0.0, (1.0 - slide) * height * -2.0, 0.0)
            "zoom" -> GlStateManager.translate(-1 * (1.0 - slide) * (width / 2.0), -1 * (1.0 - slide) * (height / 2.0), -1 * (1.0 - slide) * (width / 2.0))
            "zoombounce" -> GlStateManager.translate(-1 * (1.0 - slide) * (width / 2.0), -1 * (1.0 - slide) * (height / 2.0), 0.0)
        }

        GlStateManager.scale(1f, 1f, 1f)
        AWTFontRenderer.assumeNonVolatile = false
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()
        val wheel = Mouse.getEventDWheel()
        for (i in panels.indices.reversed())
            if (panels[i].handleScroll(mouseX, mouseY, wheel))
                break
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        var mouseX = mouseX
        var mouseY = mouseY
        val scale = MinusBounce.moduleManager[ClickGUI::class.java]!!.scaleValue.get().toDouble()

        mouseX = (mouseX / scale).toInt()
        mouseY = (mouseY / scale).toInt()

        for (i in panels.indices.reversed()) {
            if (panels[i].mouseClicked(mouseX, mouseY, mouseButton)) {
                break
            }
        }

        for (panel in panels) {
            panel.drag = false
            if (mouseButton == 0 && panel.isHovering(mouseX, mouseY)) {
                panel!!.x2 = panel.x - mouseX
                panel.y2 = panel.y - mouseY
                panel.drag = true
                panels.remove(panel)
                panels.add(panel)
                break
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        var mouseX = mouseX
        var mouseY = mouseY
        val scale = MinusBounce.moduleManager[ClickGUI::class.java]!!.scaleValue.get().toDouble()
        mouseX = (mouseX / scale).toInt()
        mouseY = (mouseY / scale).toInt()
        panels.forEach {it.mouseReleased(mouseX, mouseY, state)}

        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun updateScreen() {
        for (panel in panels) {
            for (element in panel.elements) {
                if (element is ButtonElement) {
                    if (element.isHovering(mouseX, mouseY)) {
                        if (element.hoverTime < 7) element.hoverTime++
                    } else if (element.hoverTime > 0) element.hoverTime--
                }
            }
        }
        super.updateScreen()
    }

    override fun onGuiClosed() {
        MinusBounce.fileManager.saveConfig(MinusBounce.fileManager.clickGuiConfig)
        MinusBounce.fileManager.saveConfig(MinusBounce.fileManager.valuesConfig)
    }

    fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel?) {
        RenderUtils.drawBorderedRect(panel!!.x.toFloat() - if (panel.scrollbar) 4 else 0, panel.y.toFloat(), panel.x.toFloat() + panel.width, panel.y.toFloat() + 19 + panel.getFade(), 1f, Color(255, 255, 255, 90).rgb, Int.MIN_VALUE)
        val textWidth = Fonts.font35.getStringWidth("§f" + StringUtils.stripControlCodes(panel.name)).toFloat()
        Fonts.font35.drawString("§f" + panel.name, (panel.x - (textWidth - 100.0f) / 2f).toInt(), panel.y + 7, -16777216)
        if (panel.scrollbar && panel.getFade() > 0) {
            RenderUtils.drawRect(panel.x - 1.5f, (panel.y + 21).toFloat(), panel.x - 0.5f, (panel.y + 16 + panel.getFade()).toFloat(), Int.MAX_VALUE)
            val maxElementsValue = MinusBounce.moduleManager[ClickGUI::class.java]!!.maxElementsValue.get()
            RenderUtils.drawRect((panel.x - 2).toFloat(),panel.y + 30 + (panel.getFade() - 24f) / (panel.elements.size - maxElementsValue) * panel.dragged - 10.0f, panel.x.toFloat(), panel.y + 40 + (panel.getFade() - 24.0f) / (panel.elements.size - maxElementsValue) * panel.dragged, Int.MIN_VALUE)
        }
    }

    private fun drawDescription(mouseX: Int, mouseY: Int, text: String?) {
        val textWidth = Fonts.font35.getStringWidth(text!!)
        RenderUtils.drawBorderedRect((mouseX + 9).toFloat(), mouseY.toFloat(), (mouseX + textWidth + 14).toFloat(), (mouseY + Fonts.font35.FONT_HEIGHT + 3).toFloat(), 1f, Color(255, 255, 255, 90).rgb, Int.MIN_VALUE)
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, mouseX + 12, mouseY + Fonts.font35.FONT_HEIGHT / 2, Int.MAX_VALUE)
    }

    fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement?) {
        GlStateManager.resetColor()
        Fonts.font35.drawString(buttonElement!!.displayName, (buttonElement.x + 5), buttonElement.y + 7, buttonElement.color)
    }

    private fun drawValues(moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val moduleValues = moduleElement.module.values
        if (moduleValues.isNotEmpty()) {
            Fonts.font35.drawString("+", moduleElement.x + moduleElement.width - 8, moduleElement.y + moduleElement.height / 2, Color.WHITE.rgb)
            if (moduleElement.isShowSettings) {
                yPos = moduleElement.y + 4
                for (value in moduleValues) {
                    if (!value.canDisplay.invoke()) continue
                    val isNumber = value.get() is Number || value is IntRangeValue || value is FloatRangeValue
                    when (value) {
                        is BoolValue -> drawBoolValue(value, moduleElement, mouseX, mouseY)
                        is ListValue -> drawListValue(value, moduleElement, mouseX, mouseY)
                        is FloatValue -> drawFloatValue(value, moduleElement, mouseX, mouseY)
                        is IntegerValue -> drawIntegerValue(value, moduleElement, mouseX, mouseY)
                        is FontValue -> drawFontValue(value, moduleElement, mouseX, mouseY)
                        is TextValue -> drawTextValue(value, moduleElement, mouseX, mouseY)
                        is IntRangeValue -> drawIntRangeValue(value, moduleElement, mouseX, mouseY)
                        is FloatRangeValue -> drawFloatRangeValue(value, moduleElement, mouseX, mouseY)
                    }
                    if (isNumber) {
                        AWTFontRenderer.assumeNonVolatile = true
                    }
                }
                moduleElement.updatePressed()
                mouseDown = Mouse.isButtonDown(0)
                rightMouseDown = Mouse.isButtonDown(1)
                if (moduleElement.settingsWidth > 0f && yPos > moduleElement.y + 4)
                    RenderUtils.drawBorderedRect((moduleElement.x + moduleElement.width + 4).toFloat(), (moduleElement.y + 6).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 2).toFloat(), 1f, Int.MIN_VALUE, 0)
            }
        }
    }

    fun drawModuleElement(mouseX: Int, mouseY: Int, moduleElement: ModuleElement?) {
        val guiColor = accentColor!!.rgb
        GlStateManager.resetColor()
        Fonts.font35.drawString(moduleElement!!.displayName, (moduleElement.x + 5), moduleElement.y + 7, if (moduleElement.module.state) guiColor else Int.MAX_VALUE)
        drawValues(moduleElement, mouseX, mouseY)
    }

    
    private fun drawBoolValue(value: BoolValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), (moduleElement.x + moduleElement.width + moduleElement.settingsWidth), (yPos + 14).toFloat(), Int.MIN_VALUE)

        if ((mouseX >= moduleElement.x + moduleElement.width + 4) && (mouseX <= (moduleElement.x + moduleElement.width + moduleElement.settingsWidth)) && (mouseY >= yPos + 2) && (mouseY <= yPos + 14)) {
            if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                value.set(!value.get())
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
            }
        }
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, if (value.get()) guiColor else Int.MAX_VALUE)
        yPos += 12
    }

    private fun drawListValue(value: ListValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 16) moduleElement.settingsWidth = textWidth + 16
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 14).toFloat(), Int.MIN_VALUE)
        GlStateManager.resetColor()
        Fonts.font35.drawString("§c$text", moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        Fonts.font35.drawString(if (value.openList) "-" else "+", (moduleElement.x + moduleElement.width + moduleElement.settingsWidth - if (value.openList) 5 else 6).toInt(), yPos + 4, 0xffffff)
        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
            if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                value.openList = !value.openList
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
            }
        }
        yPos += 12
        for (valueOfList in value.values) {
            val textWidth2 = Fonts.font35.getStringWidth(">$valueOfList").toFloat()
            if (moduleElement.settingsWidth < textWidth2 + 8) moduleElement.settingsWidth = textWidth2 + 8
            if (value.openList) {
                RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 14).toFloat(), Int.MIN_VALUE )
                if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 2 && mouseY <= yPos + 14) {
                    if (Mouse.isButtonDown(0) && moduleElement.isntPressed()) {
                        value.set(valueOfList)
                        mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("gui.button.press"), 1.0f))
                    }
                }
                GlStateManager.resetColor()
                Fonts.font35.drawString(">", moduleElement.x + moduleElement.width + 6, yPos + 4, Int.MAX_VALUE)
                Fonts.font35.drawString( valueOfList, moduleElement.x + moduleElement.width + 14, yPos + 4, if (value.get().equals(valueOfList, ignoreCase = true) ) guiColor else Int.MAX_VALUE)
                yPos += 12
            }
        }
    }

    private fun drawFloatValue(value: FloatValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: §c" + round(value.get()) + value.suffix
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 24).toFloat(), Int.MIN_VALUE )
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 8).toFloat(), (yPos + 18).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4, (yPos + 19).toFloat(), Int.MAX_VALUE )
        val sliderValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawRect(8 + sliderValue, (yPos + 15).toFloat(), sliderValue + 11, (yPos + 21).toFloat(), guiColor )
        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4 && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
            val dWheel = Mouse.getDWheel()
            if (Mouse.hasWheel() && dWheel != 0) {
                if (dWheel > 0) value.set(min((value.get() + 0.01f).toDouble(), value.maximum.toDouble()))
                if (dWheel < 0) value.set(max((value.get() - 0.01f).toDouble(), value.minimum.toDouble()))
            }
            if (Mouse.isButtonDown(0)) {
                val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                value.set(round((value.minimum + (value.maximum - value.minimum) * i).toFloat()))
            }
        }
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 22
    }

    private fun drawIntegerValue(value: IntegerValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: §c" + if (value is BlockValue) BlockUtils.getBlockName(value.get()) + " (" + value.get() + ")" else value.get().toString() + value.suffix
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 24).toFloat(), Int.MIN_VALUE)
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 8).toFloat(), (yPos + 18).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4, (yPos + 19).toFloat(), Int.MAX_VALUE)
        val sliderValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawRect(8 + sliderValue, (yPos + 15).toFloat(), sliderValue + 11, (yPos + 21).toFloat(), guiColor)
        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
            val dWheel = Mouse.getDWheel()
            if (Mouse.hasWheel() && dWheel != 0) {
                if (dWheel > 0) value.set(min((value.get() + 1).toDouble(), value.maximum.toDouble()))
                if (dWheel < 0) value.set(max((value.get() - 1).toDouble(), value.minimum.toDouble()))
            }
            if (Mouse.isButtonDown(0)) {
                val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                value.set((value.minimum + (value.maximum - value.minimum) * i).toInt())
            }
        }
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 22
    }

    private fun drawFontValue(value: FontValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val fontRenderer = value.get()
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 14).toFloat(), Int.MIN_VALUE)
        var displayString = "Font: Unknown"
        if (fontRenderer is GameFontRenderer) {
            displayString = "Font: " + fontRenderer.defaultFont.font.name + " - " + fontRenderer.defaultFont.font.size
        } else if (fontRenderer === Fonts.minecraftFont) displayString = "Font: Minecraft" else {
            val objects = Fonts.getFontDetails(fontRenderer)
            if (objects != null) {
                displayString = objects[0].toString() + if (objects[1] as Int != -1) " - " + objects[1] else ""
            }
        }
        Fonts.font35.drawString(displayString, moduleElement.x + moduleElement.width + 6, yPos + 4, Color.WHITE.rgb)
        val stringWidth = Fonts.font35.getStringWidth(displayString)
        if (moduleElement.settingsWidth < stringWidth + 8) moduleElement.settingsWidth = (stringWidth + 8).toFloat()
        if ((Mouse.isButtonDown(0) && !mouseDown || Mouse.isButtonDown(1) && !rightMouseDown) && mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 4 && mouseY <= yPos + 12) {
            val fonts = Fonts.fonts
            if (Mouse.isButtonDown(0)) {
                var i = 0
                while (i < fonts.size) {
                    val font = fonts[i]
                    if (font === fontRenderer) {
                        i++
                        if (i >= fonts.size) i = 0
                        value.set(fonts[i])
                        break
                    }
                    i++
                }
            } else {
                var i = fonts.size - 1
                while (i >= 0) {
                    val font = fonts[i]
                    if (font === fontRenderer) {
                        i--
                        if (i >= fonts.size) i = 0
                        if (i < 0) i = fonts.size - 1
                        value.set(fonts[i])
                        break
                    }
                    i--
                }
            }
        }
        yPos += 11
    }
    
    private fun drawTextValue(value: TextValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: §c" + value.get()
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 14).toFloat(), Int.MIN_VALUE)
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 12
    }

    private fun drawIntRangeValue(value: IntRangeValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: §c${value.get().getMin()} - ${value.get().getMax()}"
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 24).toFloat(), Int.MIN_VALUE)
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 8).toFloat(), (yPos + 18).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4, (yPos + 19).toFloat(), Int.MAX_VALUE)
        val sliderMinValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get().getMin() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawRect(8 + sliderMinValue, (yPos + 15).toFloat(), sliderMinValue + 10, (yPos + 21).toFloat(), guiColor)
        val sliderMaxValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get().getMax() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawRect(8 + sliderMaxValue, (yPos + 15).toFloat(), sliderMaxValue + 11, (yPos + 21).toFloat(), guiColor)
        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
            val dWheel = Mouse.getDWheel()

            if ((mouseX >= sliderMaxValue + 12 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4) || (mouseX >= moduleElement.x + moduleElement.width + moduleElement.settingsWidth / 2 - 2 && mouseX <= sliderMaxValue + 14)) {
                if (Mouse.hasWheel() && dWheel != 0) {
                    if (dWheel > 0) value.setMaxValue(min((value.get().getMax() + 1).toDouble(), value.maximum.toDouble()))
                    if (dWheel < 0) value.setMaxValue(max((value.get().getMax() - 1).toDouble(), value.minimum.toDouble()))
                }
                if (Mouse.isButtonDown(0)) {
                    val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                    value.setMaxValue((value.minimum + (value.maximum - value.minimum) * i).toInt())
                }
            } else if ((mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= sliderMinValue + 11) || (mouseX >= sliderMinValue + 8 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth / 2 - 2)) {
                if (Mouse.hasWheel() && dWheel != 0) {
                    if (dWheel > 0) value.setMinValue(min((value.get().getMin() + 1).toDouble(), value.maximum.toDouble()))
                    if (dWheel < 0) value.setMinValue(max((value.get().getMin() - 1).toDouble(), value.minimum.toDouble()))
                }
                if (Mouse.isButtonDown(0)) {
                    val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                    value.setMinValue((value.minimum + (value.maximum - value.minimum) * i).toInt())
                }
            }
        }
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 22
    }

    private fun drawFloatRangeValue(value: FloatRangeValue, moduleElement: ModuleElement, mouseX: Int, mouseY: Int) {
        val text = value.name + "§f: §c${round(value.get().getMin())}${value.suffix} - ${round(value.get().getMax())}${value.suffix}"
        val textWidth = Fonts.font35.getStringWidth(text).toFloat()
        if (moduleElement.settingsWidth < textWidth + 8) moduleElement.settingsWidth = textWidth + 8
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 4).toFloat(), (yPos + 2).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth, (yPos + 24).toFloat(), Int.MIN_VALUE)
        RenderUtils.drawRect((moduleElement.x + moduleElement.width + 8).toFloat(), (yPos + 18).toFloat(), moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4, (yPos + 19).toFloat(), Int.MAX_VALUE)
        val sliderMinValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get().getMin() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawRect(8 + sliderMinValue, (yPos + 15).toFloat(), sliderMinValue + 10, (yPos + 21).toFloat(), guiColor)
        val sliderMaxValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (value.get().getMax() - value.minimum) / (value.maximum - value.minimum)
        RenderUtils.drawRect(8 + sliderMaxValue, (yPos + 15).toFloat(), sliderMaxValue + 11, (yPos + 21).toFloat(), guiColor)
        if (mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth && mouseY >= yPos + 15 && mouseY <= yPos + 21) {
            val dWheel = Mouse.getDWheel()

            if ((mouseX >= sliderMaxValue + 12 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth - 4) || (mouseX >= moduleElement.x + moduleElement.width + moduleElement.settingsWidth / 2 - 2 && mouseX <= sliderMaxValue + 14)) {
                if (Mouse.hasWheel() && dWheel != 0) {
                    if (dWheel > 0) value.setMaxValue(min((value.get().getMax() + 0.01f).toDouble(), value.maximum.toDouble()))
                    if (dWheel < 0) value.setMaxValue(max((value.get().getMax() - 0.01f).toDouble(), value.minimum.toDouble()))
                }
                if (Mouse.isButtonDown(0)) {
                    val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                    value.setMaxValue(round((value.minimum + (value.maximum - value.minimum) * i).toFloat()))
                }
            } else if ((mouseX >= moduleElement.x + moduleElement.width + 4 && mouseX <= sliderMinValue + 11) || (mouseX >= sliderMinValue + 8 && mouseX <= moduleElement.x + moduleElement.width + moduleElement.settingsWidth / 2 - 2)) {
                if (Mouse.hasWheel() && dWheel != 0) {
                    if (dWheel > 0) value.setMinValue(min((value.get().getMin() + 0.01f).toDouble(), value.maximum.toDouble()))
                    if (dWheel < 0) value.setMinValue(max((value.get().getMin() - 0.01f).toDouble(), value.minimum.toDouble()))
                }
                if (Mouse.isButtonDown(0)) {
                    val i = MathHelper.clamp_double(((mouseX - moduleElement.x - moduleElement.width - 8) / (moduleElement.settingsWidth - 12)).toDouble(), 0.0, 1.0)
                    value.setMinValue(round((value.minimum + (value.maximum - value.minimum) * i).toFloat()))
                }
            }
        }
        GlStateManager.resetColor()
        Fonts.font35.drawString(text, moduleElement.x + moduleElement.width + 6, yPos + 4, 0xffffff)
        yPos += 22
    }
}