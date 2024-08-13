/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color
import java.util.*
import java.util.regex.Pattern
import kotlin.math.*

object ColorUtils {

    private val startTime = System.currentTimeMillis()
    private val COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]")

    @JvmField
    val hexColors = (0..15).map {
            val baseColor = (it shr 3 and 1) * 85

            val red = (it shr 2 and 1) * 170 + baseColor + if (it == 6) 85 else 0
            val green = (it shr 1 and 1) * 170 + baseColor
            val blue = (it and 1) * 170 + baseColor

            (red and 255 shl 16) or (green and 255 shl 8) or (blue and 255)
        }.toTypedArray()

    @JvmStatic
    fun stripColor(input: String?): String? {
        return COLOR_PATTERN.matcher(input ?: return null).replaceAll("")
    }

    @JvmStatic
    fun interpolateColor(color1: Color, color2: Color, pAmount: Float): Color {
        val amount = pAmount.coerceIn(0f, 1f)
        return Color(
            MathUtils.interpolate(color1.red, color2.red, amount.toDouble()),
            MathUtils.interpolate(color1.green, color2.green, amount.toDouble()),
            MathUtils.interpolate(color1.blue, color2.blue, amount.toDouble()),
            MathUtils.interpolate(color1.alpha, color2.alpha, amount.toDouble())
        )
    }

    @JvmStatic
    fun translateAlternateColorCodes(textToTranslate: String): String {
        val chars = textToTranslate.toCharArray()

        for (i in 0 until chars.size - 1) {
            if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(chars[i + 1], true)) {
                chars[i] = '§'
                chars[i + 1] = Character.toLowerCase(chars[i + 1])
            }
        }

        return String(chars)
    }

    fun randomMagicText(text: String): String {
        val stringBuilder = StringBuilder()
        val allowedCharacters = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000"

        for (c in text.toCharArray()) {
            if (ChatAllowedCharacters.isAllowedCharacter(c)) {
                val index = Random().nextInt(allowedCharacters.length)
                stringBuilder.append(allowedCharacters.toCharArray()[index])
            }
        }

        return stringBuilder.toString()
    }

    fun getColor(hueoffset: Float, saturation: Float, brightness: Float): Int {
        val hue = System.currentTimeMillis() % 4500 / 4500f
        return Color.HSBtoRGB(hue - hueoffset / 54, saturation, brightness)
    }

    @JvmStatic
    fun getColor(n: Int) = when (n) {
        2 -> "\u00a7a"
        3 -> "\u00a73"
        4 -> "\u00a74"
        5 -> "\u00a7e"
        else -> "\u00a7f"
    }

    @JvmStatic
    fun hoverColor(color: Color, hover: Int): Color {
        val red = max(color.red - hover * 2, 0)
        val green = max(color.green - hover * 2, 0)
        val blue = max(color.blue - hover * 2, 0)
        return Color(red, green, blue, color.alpha)
    }

    @JvmStatic
    fun reAlpha(color: Color, alpha: Int) = Color(color.red, color.green, color.blue, alpha.coerceIn(0, 255))

    @JvmStatic
    fun reAlpha(color: Color, alpha: Float) = Color(color.red / 255F, color.green / 255F, color.blue / 255F, alpha.coerceIn(0F, 1F))

    @JvmStatic
    fun getOppositeColor(color: Color) = Color(255 - color.red, 255 - color.green, 255 - color.blue, color.alpha)

    fun colorCode(code: String, alpha: Int = 255) = when (code.lowercase()) {
        "0" -> Color(0, 0, 0, alpha)
        "1" -> Color(0, 0, 170, alpha)
        "2" -> Color(0, 170, 0, alpha)
        "3" -> Color(0, 170, 170, alpha)
        "4" -> Color(170, 0, 0, alpha)
        "5" -> Color(170, 0, 170, alpha)
        "6" -> Color(255, 170, 0, alpha)
        "7" -> Color(170, 170, 170, alpha)
        "8" -> Color(85, 85, 85, alpha)
        "9" -> Color(85, 85, 255, alpha)
        "a" -> Color(85, 255, 85, alpha)
        "b" -> Color(85, 255, 255, alpha)
        "c" -> Color(255, 85, 85, alpha)
        "d" -> Color(255, 85, 255, alpha)
        "e" -> Color(255, 255, 85, alpha)
        else -> Color(255, 255, 255, alpha)
    }

    fun getGradientOffset(one: Color, two: Color, offset: Double, alpha: Int): Color {
        val offset = if (offset > 1) {
            val off = offset.toInt()
            if (off % 2 == 0) offset else 1 - offset
        } else offset

        val inverse_percent = 1 - offset

        val redPart = one.red * inverse_percent + two.red * offset
        val greenPart = one.green * inverse_percent + two.green * offset
        val bluePart = one.blue * inverse_percent + two.blue * offset

        return Color(redPart.toInt(), greenPart.toInt(), bluePart.toInt(), alpha)
    }

    fun interpolateColorWithProgress(startColor: Int, endColor: Int, progress: Float): Int {
        val startAlpha = startColor ushr 24
        val startRed = startColor shr 16 and 0xFF
        val startGreen = startColor shr 8 and 0xFF
        val startBlue = startColor and 0xFF

        val endAlpha = endColor ushr 24
        val endRed = endColor shr 16 and 0xFF
        val endGreen = endColor shr 8 and 0xFF
        val endBlue = endColor and 0xFF

        val progressLeft = 1.0 - progress

        val alpha = (progressLeft * startAlpha + progress * endAlpha).toInt()
        val red = (progressLeft * startRed + progress * endRed).toInt()
        val green = (progressLeft * startGreen + progress * endGreen).toInt()
        val blue = (progressLeft * startBlue + progress * endBlue).toInt()
        return alpha shl 24 or (red shl 16) or (green shl 8) or blue
    }

    /**
     * Color styles
     */

    @JvmStatic
    fun rainbow() = rainbow(400000L)

    @JvmStatic
    fun rainbow(offset: Long) = rainbow(offset, 1f)

    @JvmStatic
    fun rainbow(alpha: Int): Color = rainbow(alpha / 255)

    @JvmStatic
    fun rainbow(alpha: Float) = rainbow(400000L, alpha)

    @JvmStatic
    fun rainbow(offset: Long, alpha: Int) = rainbow(offset, alpha.toFloat() / 255)

    @JvmStatic
    fun rainbow(offset: Long, alpha: Float): Color {
        val hue = (System.nanoTime() + offset) / 1e10f % 1
        val hueColor = Color(Color.HSBtoRGB(hue, 1f, 1f))
        return Color(hueColor.red / 255F, hueColor.green / 255F, hueColor.blue / 255F, alpha)
    }

    @JvmStatic
    fun getTwoRainbowColor(offset: Long, alpha: Float): Color {
        val hsbColor = Color(Color.HSBtoRGB((System.nanoTime() + offset) / 8.9999999E10F % 1, 0.75F, 0.8F))
        return Color(hsbColor.getRed() / 255f, hsbColor.getGreen() / 255f, hsbColor.getBlue() / 255f, alpha)
    }

    @JvmStatic
    fun getFadeColor(color: Color, index: Int, count: Int): Color {
        val hsb = FloatArray(3)
        Color.RGBtoHSB(color.red, color.green, color.blue, hsb)
        var brightness = abs(((System.currentTimeMillis() % 2000L).toFloat() / 1000f + index / count * 2f) % 2f - 1f)
        brightness = 0.5f + 0.5f * brightness
        hsb[2] = brightness % 2.0f
        return Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]))
    }

    fun getSkyRainbowColor(hue: Int, saturation: Float, brightness: Float): Color {
        var v1 = (System.currentTimeMillis() + hue * 109) / 5.0
        return Color.getHSBColor(if ((360.0.also { v1 %= it } / 360.0).toFloat().toDouble() < 0.5) -(v1 / 360.0).toFloat() else (v1 / 360.0).toFloat(), saturation, brightness)
    }

    fun getRainbowOpaque(seconds: Int, saturation: Float, brightness: Float, index: Int): Color {
        val hue = (System.currentTimeMillis() + index) % (seconds * 1000) / (seconds * 1000).toFloat()
        return Color(Color.HSBtoRGB(hue, saturation, brightness))
    }

    fun getNormalRainbow(delay: Int, saturation: Float, brightness: Float): Color {
        val rainbowState = ceil((System.currentTimeMillis() + delay) / 20.0) % 360
        return Color.getHSBColor((rainbowState / 360.0f).toFloat(), saturation, brightness)
    }

    @JvmStatic
    fun getLiquidSlowlyColor(count: Int, saturation: Float, brightness: Float): Color {
        val hue = (System.nanoTime() - count * 3000000f) / 2e9f
        return Color(Color.HSBtoRGB(hue, saturation, brightness))
    }

    fun getEntityColor(entity: Entity?, team: Boolean = false, health: Boolean = false, hurtTime: Boolean = false): Color? {
        if (entity is EntityLivingBase) {
            if (health)
                return BlendUtils.getHealthColor(entity.health, entity.maxHealth)
            
            if (hurtTime && entity.hurtTime > 0)
                return Color.RED

            if (team) {
                val chars = entity.displayName.formattedText.toCharArray()
                for (i in chars.indices) {
                    if (chars[i] != '§' || i + 1 >= chars.size)
                        continue

                    val index = getColorIndex(chars[i + 1])

                    if (index in 0..15)
                        return Color(hexColors[index])
                }
            }
        }

        return null
    }

    fun getColorIndex(type: Char) = when (type) {
        in '0'..'9' -> type - '0'
        in 'a'..'f' -> type - 'a' + 10
        in 'k'..'o' -> type - 'k' + 16
        'r' -> 21
        else -> -1
    }
}
