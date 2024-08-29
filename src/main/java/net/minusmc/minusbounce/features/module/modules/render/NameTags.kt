/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.render

import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.features.module.modules.combat.AntiBot
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.utils.render.GLUtils
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.*
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.roundToInt

@ModuleInfo(name = "NameTags", spacedName = "Name Tags", description = "Changes the scale of the nametags so you can always read them.", category = ModuleCategory.RENDER)
class NameTags : Module() {

    private val modeValue = ListValue("Mode", arrayOf("LiquidBounce", "Jello"), "LiquidBounce")

    private val healthValue = BoolValue("Health", true) {modeValue.get().equals("liquidbounce", true)}
    private val healthBarValue = BoolValue("HealthBar", true) {modeValue.get().equals("liquidbounce", true)}
    private val pingValue = BoolValue("Ping", true) {modeValue.get().equals("liquidbounce", true)}
    private val distanceValue = BoolValue("Distance", false) {modeValue.get().equals("liquidbounce", true)}
    private val armorValue = BoolValue("Armor", true) {modeValue.get().equals("liquidbounce", true)}
    private val potionValue = BoolValue("Potions", true) {modeValue.get().equals("liquidbounce", true)}
    private val backgroundColorRedValue = IntegerValue("BackgroundRed", 0, 0, 255) {modeValue.get().equals("liquidbounce", true)}
    private val backgroundColorGreenValue = IntegerValue("BackgroundGreen", 0, 0, 255) {modeValue.get().equals("liquidbounce", true)}
    private val backgroundColorBlueValue = IntegerValue("BackgroundBlue", 0, 0, 255) {modeValue.get().equals("liquidbounce", true)}
    private val backgroundColorAlphaValue = IntegerValue("BackgroundAlpha", 0, 0, 255) {modeValue.get().equals("liquidbounce", true)}
    private val borderColorRedValue = IntegerValue("BorderRed", 0, 0, 255) {modeValue.get().equals("liquidbounce", true)}
    private val borderColorGreenValue = IntegerValue("BorderGreen", 0, 0, 255) {modeValue.get().equals("liquidbounce", true)}
    private val borderColorBlueValue = IntegerValue("BorderBlue", 0, 0, 255) {modeValue.get().equals("liquidbounce", true)}
    private val borderColorAlphaValue = IntegerValue("BorderAlpha", 0, 0, 255) {modeValue.get().equals("liquidbounce", true)}

    private val jelloColorValue = BoolValue("Jello-HPColor", true).displayable { modeValue.get().equals("jello", true) }
    private val jelloAlphaValue = IntegerValue("Jello-Alpha", 170, 0, 255).displayable { modeValue.get().equals("jello", true) }

    private val clearNamesValue = BoolValue("ClearNames", false)
    private val fontValue = FontValue("Font", Fonts.font40)
    private val fontShadowValue = BoolValue("Shadow", true)
    private val borderValue = BoolValue("Border", true)
    private val localValue = BoolValue("LocalPlayer", true)
    private val nfpValue = BoolValue("NoFirstPerson", true) { localValue.get() }
    
    private val scaleValue = FloatValue("Scale", 1F, 1F, 4F, "x")

    private val inventoryBackground = ResourceLocation("textures/gui/container/inventory.png")
    private val entities = mutableListOf<EntityLivingBase>()

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        entities.clear()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !EntityUtils.isSelected(entity, false))
                continue

            if (!localValue.get() && (entity == mc.thePlayer || nfpValue.get() && mc.gameSettings.thirdPersonView == 0))
                continue

            entities.add(entity)

            val name = if (clearNamesValue.get())
                ColorUtils.stripColor(entity.displayName.unformattedText) ?: continue
            else entity.displayName.unformattedText

            renderNameTag(entity, name)
        }
    }

    private fun renderNameTag(entity: EntityLivingBase, tag: String) {
        val fontRenderer = fontValue.get()

        glPushMatrix()

        val x = MathUtils.interpolate(entity.posX, entity.lastTickPosX, mc.timer.renderPartialTicks) - mc.renderManager.renderPosX
        val y = MathUtils.interpolate(entity.posY, entity.lastTickPosY, mc.timer.renderPartialTicks) - mc.renderManager.renderPosY + entity.eyeHeight + 0.55
        val z = MathUtils.interpolate(entity.posZ, entity.lastTickPosZ, mc.timer.renderPartialTicks) - mc.renderManager.renderPosZ
        glTranslated(x, y, z)
        glRotatef(-mc.renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(mc.renderManager.playerViewX, 1F, 0F, 0F)

        var distance = (mc.thePlayer.getDistanceToEntity(entity) * 0.25f).coerceAtLeast(1f)
        val scale = distance / 100f * scaleValue.get()

        GLUtils.disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)
        GLUtils.enableGlCap(GL_LINE_SMOOTH, GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)


        when (modeValue.get().lowercase()) {
            "liquidbounce" -> {
                val isBot = AntiBot.isBot(entity)

                val nameColor = when {
                    isBot -> "§3"
                    entity.isInvisible -> "§6"
                    entity.isSneaking -> "§4"
                    else -> "§7"
                }

                val ping = if (entity is EntityPlayer) EntityUtils.getPing(entity) else 0

                val distanceText = if (distanceValue.get()) "§7 [§a${mc.thePlayer.getDistanceToEntity(entity).roundToInt()}§7]" else ""
                val pingText = if (pingValue.get() && entity is EntityPlayer) when {
                    ping > 200 -> " §7[§c${ping}ms§7]"
                    ping > 100 -> " §7[§e${ping}ms§7]"
                    else -> " §7[§a${ping}ms§7]"
                } else ""

                val healthText = if (healthValue.get()) "§7 [§f${entity.health.toInt()}§c❤§7]" else ""
                val botText = if (isBot) " §7[§6§lBot§7]" else ""

                val text = "$nameColor$tag$healthText$distanceText$pingText$botText"
                glScalef(-scale, -scale, scale)

                // Draw NameTag
                val width = fontRenderer.getStringWidth(text) * 0.5f
                val dist = width * 2 + 6f

                glDisable(GL_TEXTURE_2D)
                glEnable(GL_BLEND)

                val bgColor = Color(backgroundColorRedValue.get(), backgroundColorGreenValue.get(), backgroundColorBlueValue.get(), backgroundColorAlphaValue.get())
                val borderColor = Color(borderColorRedValue.get(), borderColorGreenValue.get(), borderColorBlueValue.get(), borderColorAlphaValue.get())

                if (borderValue.get())
                    RenderUtils.quickDrawBorderedRect(-width - 2F, -2F, width + 4F, fontRenderer.FONT_HEIGHT + 2F + if (healthBarValue.get()) 2F else 0F, 2F, borderColor.rgb, bgColor.rgb)
                else
                    RenderUtils.quickDrawRect(-width - 2F, -2F, width + 4F, fontRenderer.FONT_HEIGHT + 2F + if (healthBarValue.get()) 2F else 0F, bgColor.rgb)

                if (healthBarValue.get()) {
                    RenderUtils.quickDrawRect(-width - 2F, fontRenderer.FONT_HEIGHT + 3F, -width - 2F + dist, fontRenderer.FONT_HEIGHT + 4F, Color(10, 155, 10).rgb)
                    RenderUtils.quickDrawRect(-width - 2F, fontRenderer.FONT_HEIGHT + 3F, -width - 2F + dist * (entity.health / entity.maxHealth).coerceIn(0F, 1F), fontRenderer.FONT_HEIGHT + 4F, Color(10, 255, 10).rgb)
                }

                glEnable(GL_TEXTURE_2D)

                fontRenderer.drawString(text, 1f - width, if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F, 0xFFFFFF, fontShadowValue.get())

                var foundPotion = false
                if (potionValue.get() && entity is EntityPlayer) {
                    val potions = entity.activePotionEffects
                        .map { Potion.potionTypes[it.potionID] }
                        .filter { it.hasStatusIcon() }

                    if (potions.isNotEmpty()) {
                        foundPotion = true

                        color(1.0F, 1.0F, 1.0F, 1.0F)
                        disableLighting()
                        enableTexture2D()

                        val minX = (potions.size * -20) / 2

                        glPushMatrix()
                        enableRescaleNormal()

                        potions.forEachIndexed {index, potion ->
                            color(1f, 1f, 1f, 1f)
                            mc.textureManager.bindTexture(inventoryBackground)
                            val i1 = potion.statusIconIndex
                            RenderUtils.drawTexturedModalRect(minX + index * 20, -22, i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18, 0f)
                        }

                        disableRescaleNormal()
                        glPopMatrix()

                        enableAlpha()
                        disableBlend()
                        enableTexture2D()
                    }
                }

                if (armorValue.get() && entity is EntityPlayer) {
                    for (index in 0..4) {
                        val item = entity.getEquipmentInSlot(index) ?: continue

                        mc.renderItem.zLevel = -147f
                        mc.renderItem.renderItemAndEffectIntoGUI(item, index * 20 - 50, if (potionValue.get() && foundPotion) -42 else -22)
                    }

                    enableAlpha()
                    disableBlend()
                    enableTexture2D()
                }
            }
            "jello" -> {
                var hpBarColor = Color(255, 255, 255, jelloAlphaValue.get())
                val name = entity.displayName.unformattedText

                if (jelloColorValue.get() && name.startsWith("§"))
                    hpBarColor = ColorUtils.colorCode(name.substring(1, 2), jelloAlphaValue.get())

                val bgColor = Color(20, 20, 20, jelloAlphaValue.get())
                val width = fontRenderer.getStringWidth(tag) / 2
                val maxWidth = width * 2 + 8f
                var healthPercent = (entity.health / entity.maxHealth).coerceAtMost(1f)

                glScalef(-scale * 2, -scale * 2, scale * 2)
                RenderUtils.drawRect(-width - 4F, -fontRenderer.FONT_HEIGHT * 3F, width + 4F, -3F, bgColor)

                RenderUtils.drawRect(-width - 4F, -3F, -width - 4F + maxWidth * healthPercent, 0F, hpBarColor)
                RenderUtils.drawRect(-width - 4F + maxWidth * healthPercent, -3F, width + 4F, 0F, bgColor)

                fontRenderer.drawString(tag, -width, -fontRenderer.FONT_HEIGHT * 2 - 4, Color.WHITE.rgb)
                glScalef(0.5F, 0.5F, 0.5F)
                fontRenderer.drawString("Health: ${entity.health.toInt()}", -width * 2, -fontRenderer.FONT_HEIGHT * 2, Color.WHITE.rgb)
            }
        }

        GLUtils.resetCaps()
        resetColor()
        glColor4f(1F, 1F, 1F, 1F)

        // Pop
        glPopMatrix()
    }

    @EventTarget
    fun onRenderNameTags(event: RenderNameTagsEvent) {
        if (event.entity in entities)
            event.isCancelled = true
    }
}
