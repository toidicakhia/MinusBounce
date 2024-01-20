/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.hud.element.elements

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.ui.client.hud.designer.GuiHudDesigner
import net.minusmc.minusbounce.ui.client.hud.element.Border
import net.minusmc.minusbounce.ui.client.hud.element.Element
import net.minusmc.minusbounce.ui.client.hud.element.ElementInfo
import net.minusmc.minusbounce.ui.client.hud.element.Side
import net.minusmc.minusbounce.ui.client.hud.element.Side.Horizontal
import net.minusmc.minusbounce.ui.client.hud.element.Side.Vertical
import net.minusmc.minusbounce.ui.font.AWTFontRenderer
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.render.AnimationUtils
import net.minusmc.minusbounce.utils.render.BlurUtils
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.render.ShadowUtils
import net.minusmc.minusbounce.value.*
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

import org.lwjgl.opengl.GL11

/**
 * CustomHUD Arraylist element
 *
 * Shows a list of enabled modules
 */
@ElementInfo(name = "Arraylist", single = true)
class Arraylist(x: Double = 1.0, y: Double = 2.0, scale: Float = 1F,
                side: Side = Side(Horizontal.RIGHT, Vertical.UP)) : Element(x, y, scale, side) {
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Random", "Sky", "CRainbow", "LiquidSlowly", "Fade", "Jello"), "Custom")
    private val blurValue = BoolValue("Blur", false)
    private val blurStrength = FloatValue("Blur-Strength", 0F, 0F, 30F) { blurValue.get() }
    private val shadowShaderValue = BoolValue("Shadow", false)
    private val shadowNoCutValue = BoolValue("Shadow-NoCut", false)
    private val shadowStrength = IntegerValue("Shadow-Strength", 1, 1, 30) { shadowShaderValue.get() }
    private val shadowColorMode = ListValue("Shadow-Color", arrayOf("Background", "Text", "Custom"), "Background") { shadowShaderValue.get() }
    private val shadowColorRedValue = IntegerValue("Shadow-Red", 0, 0, 255) {
        shadowShaderValue.get() && shadowColorMode.get().equals("custom", true)
    }
    private val shadowColorGreenValue = IntegerValue("Shadow-Green", 111, 0, 255) {
        shadowShaderValue.get() && shadowColorMode.get().equals("custom", true)
    }
    private val shadowColorBlueValue = IntegerValue("Shadow-Blue", 255, 0, 255) {
        shadowShaderValue.get() && shadowColorMode.get().equals("custom", true)
    }
    private val colorRedValue = IntegerValue("Red", 0, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 111, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val colorAlphaValue = IntegerValue("Alpha", 255, 0, 255)
    private val saturationValue = FloatValue("Saturation", 0.9f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val skyDistanceValue = IntegerValue("Sky-Distance", 2, 0, 4)
    private val cRainbowSecValue = IntegerValue("CRainbow-Seconds", 2, 1, 10)
    private val cRainbowDistValue = IntegerValue("CRainbow-Distance", 2, 1, 6)
    private val liquidSlowlyDistanceValue = IntegerValue("LiquidSlowly-Distance", 90, 1, 90)
    private val fadeDistanceValue = IntegerValue("Fade-Distance", 50, 1, 100)
    private val hAnimation = ListValue("HorizontalAnimation", arrayOf("Default", "None", "Slide", "Astolfo"), "Default")
    private val vAnimation = ListValue("VerticalAnimation", arrayOf("None", "LiquidSense", "Slide", "Rise", "Astolfo"), "None")
    private val animationSpeed = FloatValue("Animation-Speed", 0.25F, 0.01F, 1F)
    private val nameBreak = BoolValue("NameBreak", true)
    private val abcOrder = BoolValue("Alphabetical-Order", false)
    private val tags = BoolValue("Tags", true)
    private val tagsStyleValue = ListValue("TagsStyle", arrayOf("-", "|", "()", "[]", "<>", "Default"), "-")
    private val shadow = BoolValue("ShadowText", true)
    private val backgroundColorRedValue = IntegerValue("Background-R", 0, 0, 255)
    private val backgroundColorGreenValue = IntegerValue("Background-G", 0, 0, 255)
    private val backgroundColorBlueValue = IntegerValue("Background-B", 0, 0, 255)
    private val backgroundColorAlphaValue = IntegerValue("Background-Alpha", 0, 0, 255)
    private val rectRightValue = ListValue("Rect-Right", arrayOf("None", "Left", "Right", "Outline", "Special", "Top"), "None")
    private val rectLeftValue = ListValue("Rect-Left", arrayOf("None", "Left", "Right"), "None")
    private val caseValue = ListValue("Case", arrayOf("None", "Lower", "Upper"), "None")
    private val spaceValue = FloatValue("Space", 0F, 0F, 5F)
    private val textHeightValue = FloatValue("TextHeight", 11F, 1F, 20F)
    private val textYValue = FloatValue("TextY", 1F, 0F, 20F)
    private val tagsArrayColor = BoolValue("TagsArrayColor", false)
    private val fontValue = FontValue("Font", Fonts.font40)

    private var x2 = 0
    private var y2 = 0F

    private var modules = emptyList<Module>()
    private var sortedModules = emptyList<Module>()

    private var counter = 0

    override fun drawElement(): Border? {
        val fontRenderer = fontValue.get()
        counter = 0

        AWTFontRenderer.assumeNonVolatile = true

        // Slide animation - update every render
        val delta = RenderUtils.deltaTime
        
        // Draw arraylist
        val rectColorMode = colorModeValue.get()
        val customColor = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())
        val rectCustomColor = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())
        val space = spaceValue.get()
        val textHeight = textHeightValue.get()
        val textY = textYValue.get()
        val backgroundCustomColor = Color(backgroundColorRedValue.get(), backgroundColorGreenValue.get(),
                backgroundColorBlueValue.get(), backgroundColorAlphaValue.get()).rgb
        val textShadow = shadow.get()
        val textSpacer = textHeight + space

        var inx = 0
        for (module in sortedModules) {
            // update slide x
            if (module.array && (module.state || module.slide != 0F)) {
                val displayString = getModName(module)

                val width = fontRenderer.getStringWidth(displayString)

                when (hAnimation.get().lowercase()) {
                    "astolfo" -> {
                        if (module.state) {
                            if (module.slide < width) {
                                module.slide += animationSpeed.get() * delta
                                module.slideStep = delta / 1F
                            }
                        } else if (module.slide > 0) {
                            module.slide -= animationSpeed.get() * delta
                            module.slideStep = 0F
                        }

                        if (module.slide > width) module.slide = width.toFloat()
                    }
                    "slide" -> {
                        if (module.state) {
                            if (module.slide < width) {
                                module.slide = net.minusmc.minusbounce.utils.render.AnimationUtils.animate(width.toDouble(), module.slide.toDouble(), animationSpeed.get().toDouble() * 0.025 * delta.toDouble()).toFloat()
                                module.slideStep = delta / 1F
                            }
                        } else if (module.slide > 0) {
                            module.slide = net.minusmc.minusbounce.utils.render.AnimationUtils.animate(-width.toDouble(), module.slide.toDouble(), animationSpeed.get().toDouble() * 0.025 * delta.toDouble()).toFloat()
                            module.slideStep = 0F
                        }
                    }
                    "default" -> {
                        if (module.state) {
                            if (module.slide < width) {
                                module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                                module.slideStep += delta / 4F
                            }
                        } else if (module.slide > 0) {
                            module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                            module.slideStep -= delta / 4F
                        }
                    }
                    else -> {
                        module.slide = if (module.state) width.toFloat() else 0f
                        module.slideStep += (if (module.state) delta else -delta).toFloat()
                    }
                }

                module.slide = module.slide.coerceIn(0F, width.toFloat())
                module.slideStep = module.slideStep.coerceIn(0F, width.toFloat())
            }

            // update slide y
            var yPos = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                            if (side.vertical == Vertical.DOWN) inx + 1 else inx
            
            if (module.array && module.slide > 0F) {
                if (vAnimation.get().equals("Rise", ignoreCase = true) && !module.state) 
                    yPos = -fontRenderer.FONT_HEIGHT - textY

                val size = modules.size * 2.0E-2f

                when (vAnimation.get().lowercase()) {
                    "liquidsense" -> {
                        if (module.state) {
                            if (module.arrayY < yPos) {
                                module.arrayY += (size -
                                        (module.arrayY * 0.002f).coerceAtMost(size - (module.arrayY * 0.0001f))) * delta
                                module.arrayY = yPos.coerceAtMost(module.arrayY)
                            } else {
                                module.arrayY -= (size -
                                        (module.arrayY * 0.002f).coerceAtMost(size - (module.arrayY * 0.0001f))) * delta
                                module.arrayY = module.arrayY.coerceAtLeast(yPos)
                            }
                        }
                    }
                    "slide", "rise" -> module.arrayY = net.minusmc.minusbounce.utils.render.AnimationUtils.animate(yPos.toDouble(), module.arrayY.toDouble(), animationSpeed.get().toDouble() * 0.025 * delta.toDouble()).toFloat()
                    "astolfo" -> {
                        if (module.arrayY < yPos) {
                            module.arrayY += animationSpeed.get() / 2F * delta
                            module.arrayY = yPos.coerceAtMost(module.arrayY)
                        } else {
                            module.arrayY -= animationSpeed.get() / 2F * delta
                            module.arrayY = module.arrayY.coerceAtLeast(yPos)
                        }
                    }
                    else -> module.arrayY = yPos
                }
                inx++
            } else if (!vAnimation.get().equals("rise", true)) //instant update
                module.arrayY = yPos
        }

        when (side.horizontal) {
            Horizontal.RIGHT, Horizontal.MIDDLE -> {
                if (shadowShaderValue.get()) {
                    GL11.glTranslated(-renderX, -renderY, 0.0)
                    GL11.glPushMatrix()
                    ShadowUtils.shadow(shadowStrength.get().toFloat(), {
                        GL11.glPushMatrix()
                        GL11.glTranslated(renderX, renderY, 0.0)
                        modules.forEachIndexed { index, module ->
                            val xPos = -module.slide - 2
                            val color = when (shadowColorMode.get().lowercase()) {
                                "background" -> Color(backgroundColorRedValue.get(), backgroundColorGreenValue.get(), backgroundColorBlueValue.get()).rgb
                                "text" -> getColor(module, index, customColor)
                                else -> Color(shadowColorRedValue.get(), shadowColorGreenValue.get(), shadowColorBlueValue.get()).rgb
                            }
                            if (shadowColorMode.equals("text")) counter--
                            RenderUtils.newDrawRect(xPos - if (rectRightValue.equals("right")) 3 else 2, module.arrayY, if (rectRightValue.equals("right")) -1F else 0F, module.arrayY + textHeight, color)
                        }
                        GL11.glPopMatrix()
                        counter = 0
                    }, {
                        if (!shadowNoCutValue.get()) {
                            GL11.glPushMatrix()
                            GL11.glTranslated(renderX, renderY, 0.0)
                            modules.forEachIndexed { _, module ->
                                val xPos = -module.slide - 2
                                RenderUtils.quickDrawRect(xPos - if (rectRightValue.equals("right")) 3 else 2, module.arrayY, if (rectRightValue.equals("right")) -1F else 0F, module.arrayY + textHeight)
                            }
                            GL11.glPopMatrix()
                        }
                    })
                    GL11.glPopMatrix()
                    GL11.glTranslated(renderX, renderY, 0.0)
                }

                if (blurValue.get()) {
                    GL11.glTranslated(-renderX, -renderY, 0.0)
                    GL11.glPushMatrix()
                    val floatX = renderX.toFloat()
                    val floatY = renderY.toFloat()
                    var yP = 0F
                    var xP = 0F
                    modules.forEachIndexed { index, module -> 
                        val dString = getModName(module)
                        val wid = fontRenderer.getStringWidth(dString) + 2F
                        val yPos = if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer * if (side.vertical == Vertical.DOWN) index + 1 else index
                        yP += yPos
                        xP = xP.coerceAtMost(-wid)
                    }

                    BlurUtils.blur(floatX, floatY, floatX + xP, floatY + yP, blurStrength.get(), false) {
                        modules.forEachIndexed { _, module ->
                            val xPos = -module.slide - 2
                            RenderUtils.quickDrawRect(floatX + xPos - if (rectRightValue.equals("right")) 3 else 2, floatY + module.arrayY, floatX + if (rectRightValue.equals("right")) -1F else 0F, floatY + module.arrayY + textHeight)
                        }
                    }
                    GL11.glPopMatrix()
                    GL11.glTranslated(renderX, renderY, 0.0)
                }

                modules.forEachIndexed { index, module ->
                    val displayString = getModName(module)
                    val xPos = -module.slide - 2
                    val color = getColor(module, index, customColor)
                    counter--
                    RenderUtils.drawRect(xPos - if (rectRightValue.equals("right")) 3 else 2, module.arrayY, if (rectRightValue.equals("right")) -1F else 0F, module.arrayY + textHeight, backgroundCustomColor)
                    fontRenderer.drawString(displayString, xPos - if (rectRightValue.equals("right")) 1 else 0, module.arrayY + textY, color, textShadow)
                    
                    val rectColor = getColor(module, index, rectCustomColor)
                    when (rectRightValue.get().lowercase()) {
                        "left" -> RenderUtils.drawRect(xPos - 3, module.arrayY, xPos - 2, module.arrayY + textHeight, rectColor)
                        "right" -> RenderUtils.drawRect(-1F, module.arrayY, 0F, module.arrayY + textHeight, rectColor)
                        "outline" -> {                          
                            RenderUtils.drawRect(-1F, module.arrayY - 1F, 0F, module.arrayY + textHeight, rectColor)
                            RenderUtils.drawRect(xPos - 3, module.arrayY, xPos - 2, module.arrayY + textHeight, rectColor)
                            if (module != modules[0]) {
                                val displayStrings = getModName(modules[index - 1])
                                RenderUtils.drawRect(xPos - 3 - (fontRenderer.getStringWidth(displayStrings) - fontRenderer.getStringWidth(displayString)), module.arrayY, xPos - 2, module.arrayY + 1, rectColor)
                                if (module == modules[modules.size - 1]) {
                                    RenderUtils.drawRect(xPos - 3, module.arrayY + textHeight, 0.0F, module.arrayY + textHeight + 1, rectColor)
                                }
                            } else
                                RenderUtils.drawRect(xPos - 3, module.arrayY, 0F, module.arrayY - 1, rectColor)
                        }
                        "special" -> {
                            if (module == modules[0])
                                RenderUtils.drawRect(xPos - 2, module.arrayY, 0F, module.arrayY - 1, rectColor)
                            if (module == modules[modules.size - 1])
                                RenderUtils.drawRect(xPos - 2, module.arrayY + textHeight, 0F, module.arrayY + textHeight + 1, rectColor)
                        }
                        "top" -> if (module == modules[0]) RenderUtils.drawRect(xPos - 2, module.arrayY, 0F, module.arrayY - 1, rectColor)
                    }
                }
            }

            Horizontal.LEFT -> {
                if (shadowShaderValue.get()) {
                    GL11.glTranslated(-renderX, -renderY, 0.0)
                    GL11.glPushMatrix()
                    ShadowUtils.shadow(shadowStrength.get().toFloat(), {
                        GL11.glPushMatrix()
                        GL11.glTranslated(renderX, renderY, 0.0)
                        modules.forEachIndexed { index, module ->
                            val displayString = getModName(module)
                            val width = fontRenderer.getStringWidth(displayString)
                            val xPos = -(width - module.slide) + if (rectLeftValue.equals("left")) 3 else 2

                            val color = when (shadowColorMode.get().lowercase()) {
                                "background" -> Color(backgroundColorRedValue.get(), backgroundColorGreenValue.get(), backgroundColorBlueValue.get()).rgb
                                "text" -> getColor(module, index, customColor)
                                else -> Color(shadowColorRedValue.get(), shadowColorGreenValue.get(), shadowColorBlueValue.get()).rgb
                            }
                            if (shadowColorMode.equals("text")) counter--

                            RenderUtils.newDrawRect(0F, module.arrayY, xPos + width + if (rectLeftValue.equals("left")) 3F else 2F, module.arrayY + textHeight, color)
                        }
                        GL11.glPopMatrix()
                    }, {
                        if (!shadowNoCutValue.get()) {
                            GL11.glPushMatrix()
                            GL11.glTranslated(renderX, renderY, 0.0)
                            modules.forEachIndexed { _, module ->
                                val displayString = getModName(module)
                                val width = fontRenderer.getStringWidth(displayString)
                                val xPos = -(width - module.slide) + if (rectLeftValue.equals("left")) 3 else 2
                                RenderUtils.quickDrawRect(0F, module.arrayY, xPos + width + if (rectLeftValue.equals("left")) 3 else 2, module.arrayY + textHeight)
                            }
                            GL11.glPopMatrix()
                        }
                    })
                    GL11.glPopMatrix()
                    GL11.glTranslated(renderX, renderY, 0.0)
                }

                if (blurValue.get()) {
                    GL11.glTranslated(-renderX, -renderY, 0.0)
                    GL11.glPushMatrix()
                    val floatX = renderX.toFloat()
                    val floatY = renderY.toFloat()
                    var yP = 0F
                    var xP = 0F
                    modules.forEachIndexed { index, module -> 
                        val dString = getModName(module)
                        val wid = fontRenderer.getStringWidth(dString) + 2F
                        val yPos = if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer * if (side.vertical == Vertical.DOWN) index + 1 else index
                        yP += yPos
                        xP = xP.coerceAtLeast(wid)
                    }

                    BlurUtils.blur(floatX, floatY, floatX + xP, floatY + yP, blurStrength.get(), false) {
                        modules.forEachIndexed { _, module ->
                            val displayString = getModName(module)
                            val width = fontRenderer.getStringWidth(displayString)
                            val xPos = -(width - module.slide) + if (rectLeftValue.equals("left")) 3 else 2
                            RenderUtils.quickDrawRect(floatX, floatY + module.arrayY, floatX + xPos + width + if (rectLeftValue.equals("left")) 3 else 2, floatY + module.arrayY + textHeight)
                        }
                    }
                    GL11.glPopMatrix()
                    GL11.glTranslated(renderX, renderY, 0.0)
                }

                modules.forEachIndexed { index, module ->
                    val displayString = getModName(module)

                    val width = fontRenderer.getStringWidth(displayString)
                    val xPos = -(width - module.slide) + if (rectLeftValue.equals("left")) 3 else 2
                    val color = getColor(module, index, customColor)
                    counter--
                    RenderUtils.drawRect(0F, module.arrayY, xPos + width + if (rectLeftValue.equals("left")) 3 else 2, module.arrayY + textHeight, backgroundCustomColor)
                    fontRenderer.drawString(displayString, xPos, module.arrayY + textY, color, textShadow)
                    
                    val rectColor = getColor(module, index, rectCustomColor)
                    when (rectLeftValue.get().lowercase()) {
                        "left" -> RenderUtils.drawRect(0F, module.arrayY - 1, 1F, module.arrayY + textHeight, rectColor)
                        "right" -> RenderUtils.drawRect(xPos + width + 2, module.arrayY, xPos + width + 2 + 1, module.arrayY + textHeight, rectColor)
                    }
                }
            }
        }

        // Draw border
        if (mc.currentScreen is GuiHudDesigner) {
            x2 = Int.MIN_VALUE

            if (modules.isEmpty()) {
                return if (side.horizontal == Horizontal.LEFT)
                    Border(0F, -1F, 20F, 20F)
                else
                    Border(0F, -1F, -20F, 20F)
            }

            for (module in modules) {
                when (side.horizontal) {
                    Horizontal.RIGHT, Horizontal.MIDDLE -> {
                        val xPos = -module.slide.toInt() - 2
                        if (x2 == Int.MIN_VALUE || xPos < x2) x2 = xPos
                    }
                    Horizontal.LEFT -> {
                        val xPos = module.slide.toInt() + 14
                        if (x2 == Int.MIN_VALUE || xPos > x2) x2 = xPos
                    }
                }
            }
            y2 = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * modules.size

            return Border(0F, 0F, x2 - 7F, y2 - if (side.vertical == Vertical.DOWN) 1F else 0F)
        }

        AWTFontRenderer.assumeNonVolatile = false
        GlStateManager.resetColor()
        return null
    }

    override fun updateElement() {
        modules = if (abcOrder.get()) MinusBounce.moduleManager.modules
                .filter { it.array && (if (hAnimation.get().equals("none", ignoreCase = true)) it.state else it.slide > 0) }
                else MinusBounce.moduleManager.modules
                .filter { it.array && (if (hAnimation.get().equals("none", ignoreCase = true)) it.state else it.slide > 0) }
                .sortedBy { -fontValue.get().getStringWidth(getModName(it)) }
        sortedModules = if (abcOrder.get()) MinusBounce.moduleManager.modules.toList()
                else MinusBounce.moduleManager.modules.sortedBy { -fontValue.get().getStringWidth(getModName(it)) }.toList()
    }

    private fun getModTag(m: Module): String {
        if (!tags.get() || m.tag == null) return ""

        var returnTag = " ${if (tagsArrayColor.get()) "" else "§7"}"

        // tag prefix, ignore default value
        if (!tagsStyleValue.get().equals("default", true)) 
            returnTag += tagsStyleValue.get()[0].toString() + if (tagsStyleValue.get().equals("-", true) || tagsStyleValue.get().equals("|", true)) " " else ""

        // main tag value
        returnTag += m.tag

        // tag suffix, ignore default, -, | values
        if (!tagsStyleValue.get().equals("default", true) 
            && !tagsStyleValue.get().equals("-", true) 
            && !tagsStyleValue.get().equals("|", true)) 
            returnTag += tagsStyleValue.get()[1].toString()

        return returnTag
    }

    private fun getModName(mod: Module): String {
        var displayName : String = (if (nameBreak.get()) mod.spacedName else mod.name) + getModTag(mod)

        when (caseValue.get().lowercase()) {
            "lower" -> displayName = displayName.lowercase()
            "upper" -> displayName = displayName.uppercase()
        }
        
        return displayName        
    }

    private fun getColor(module: Module, index: Int, customColor: Color): Int = when(colorModeValue.get().lowercase()) {
        "random" -> Color.getHSBColor(module.hue, saturationValue.get(), brightnessValue.get()).rgb
        "sky" -> ColorUtils.skyRainbow(counter * (skyDistanceValue.get() * 50), saturationValue.get(), brightnessValue.get())
        "crainbow" -> ColorUtils.getRainbowOpaque(cRainbowSecValue.get(), saturationValue.get(), brightnessValue.get(), counter * (50 * cRainbowDistValue.get()))
        "liquidslowly" -> ColorUtils.fade(Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get()), index * fadeDistanceValue.get(), 100).rgb
        "fade" -> ColorUtils.liquidSlowly(System.nanoTime(), index * liquidSlowlyDistanceValue.get(), saturationValue.get(), brightnessValue.get()).rgb
        else -> customColor.rgb
    }
}