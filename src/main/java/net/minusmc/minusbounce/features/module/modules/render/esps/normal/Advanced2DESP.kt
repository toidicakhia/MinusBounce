package net.minusmc.minusbounce.features.module.modules.render.esps.normal

import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minusmc.minusbounce.event.Render2DEvent
import net.minusmc.minusbounce.features.module.modules.render.esps.ESPMode
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.item.ItemUtils
import net.minusmc.minusbounce.utils.misc.MathUtils
import net.minusmc.minusbounce.utils.render.BlendUtils
import net.minusmc.minusbounce.utils.render.ColorUtils
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.ui.font.TTFFontRenderer
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import java.awt.Color
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.text.DecimalFormat
import java.util.*
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class Advanced2DESP: ESPMode("Advanced2D") {

	val outline = BoolValue("Outline", true)
    private val boxMode = ListValue("Mode", arrayOf("Box", "Corners"), "Box")
    private val healthBarMode = ListValue("HealthBarMode", arrayOf("Dot", "Line", "None"), "None")
    private val healthNumberMode = ListValue("HealthNumber", arrayOf("Health", "Percent", "None"), "Health") { !healthBarMode.get().equals("none", true) }
    private val absorption = BoolValue("RenderAbsorption", true) { healthBarMode.get().equals("line", true)}
    
    private val armorBarMode = ListValue("ArmorBarMode", arrayOf("Total", "Items", "None"), "Total")
    private val armorNumber = BoolValue("ItemArmorNumber", true) { !armorBarMode.get().equals("none", true) }
    private val armorItems = BoolValue("ArmorItems", true)
    private val armorDurability = BoolValue("ArmorDurability", true) { armorItems.get() }

    private val hoverValue = BoolValue("OnlyHoverDetails", false)
    private val tagsValue = BoolValue("Tags", true)
    private val tagsBGValue = BoolValue("TagsBackground", true) { tagsValue.get() }
    private val itemTagsValue = BoolValue("ItemTags", true)
    private val outlineFont = BoolValue("OutlineFont", true)
    private val clearNameValue = BoolValue("UseClearName", false)
    private val fontScaleValue = FloatValue("FontScale", 0.5f, 0f, 1f, "x")
    private val localPlayer = BoolValue("LocalPlayer", true)

	private val viewport = GLAllocation.createDirectIntBuffer(16)
    private val modelview = GLAllocation.createDirectFloatBuffer(16)
    private val projection = GLAllocation.createDirectFloatBuffer(16)
    private val vector = GLAllocation.createDirectFloatBuffer(4)
    private val dFormat = DecimalFormat("0.0")
    private var scaleFactor = 0
    private var scaledResolution = ScaledResolution(mc)

    override fun onRender2D(event: Render2DEvent, color: Color) {
        GL11.glPushMatrix()
        scaledResolution = ScaledResolution(mc)
        scaleFactor = scaledResolution.scaleFactor
        val scaling = scaleFactor / scaleFactor.toDouble().pow(2.0)
        GL11.glScaled(scaling, scaling, scaling)

        val partialTicks = event.partialTicks

        for (entity in mc.theWorld.loadedEntityList) {
            val isEntitySelected = EntityUtils.isSelected(entity, true) || (localPlayer.get() && entity is EntityPlayerSP && mc.gameSettings.thirdPersonView != 0)

            if (entity is EntityLivingBase && isEntitySelected && RenderUtils.isInViewFrustrum(entity)) {
                val entityColor = esp.getEntityColor(entity)
                renderEntity(entity, partialTicks, entityColor)
            }
        }

        GL11.glPopMatrix()
        GlStateManager.enableBlend()
        GlStateManager.resetColor()
        mc.entityRenderer.setupOverlayRendering()
    }

    private fun renderEntity(entity: EntityLivingBase, partialTicks: Float, color: Color) {
        val x = MathUtils.interpolate(entity.posX, entity.lastTickPosX, partialTicks.toDouble())
        val y = MathUtils.interpolate(entity.posY, entity.lastTickPosY, partialTicks.toDouble())
        val z = MathUtils.interpolate(entity.posZ, entity.lastTickPosZ, partialTicks.toDouble())
        val width = entity.width / 1.5
        val height = entity.height + if (entity.isSneaking) -0.3 else 0.2
        val aabb = AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width)

        val vectors = arrayOf(
            Vector3d(aabb.minX, aabb.minY, aabb.minZ),
            Vector3d(aabb.minX, aabb.maxY, aabb.minZ),
            Vector3d(aabb.maxX, aabb.minY, aabb.minZ),
            Vector3d(aabb.maxX, aabb.maxY, aabb.minZ),
            Vector3d(aabb.minX, aabb.minY, aabb.maxZ),
            Vector3d(aabb.minX, aabb.maxY, aabb.maxZ),
            Vector3d(aabb.maxX, aabb.minY, aabb.maxZ),
            Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ)
        )

        mc.entityRenderer.setupCameraTransform(partialTicks, 0)

        var position: Vector4d? = null
        for (vectorItem in vectors) {
            val pos = project2D(scaleFactor, vectorItem.x - mc.renderManager.viewerPosX, vectorItem.y - mc.renderManager.viewerPosY, vectorItem.z - mc.renderManager.viewerPosZ) ?: continue

            if (pos.z >= 0.0 && pos.z < 1.0) {
                if (position == null) {
                    position = Vector4d(pos.x, pos.y, pos.z, 0.0)
                    continue
                }

                position.x = min(pos.x, position.x)
                position.y = min(pos.y, position.y)
                position.z = max(pos.x, position.z)
                position.w = max(pos.y, position.w)
            }
        }

        position ?: return

        mc.entityRenderer.setupOverlayRendering()
        val posX = position.x
        val posY = position.y
        val endPosX = position.z
        val endPosY = position.w

        val deltaX = endPosX - posX
        val deltaY = endPosY - posY

        if (outline.get()) {
            if (boxMode.get().equals("box", true)) {
                RenderUtils.newDrawRect(posX - 1.0, posY, posX + 0.5, endPosY + 0.5, Color.BLACK.rgb)
                RenderUtils.newDrawRect(posX - 1.0, posY - 0.5, endPosX + 0.5, posY + 1.0, Color.BLACK.rgb)
                RenderUtils.newDrawRect(endPosX - 1.0, posY, endPosX + 0.5, endPosY + 0.5, Color.BLACK.rgb)
                RenderUtils.newDrawRect(posX - 1.0, endPosY - 1.0, endPosX + 0.5, endPosY + 0.5, Color.BLACK.rgb)
                RenderUtils.newDrawRect(posX - 0.5, posY, posX, endPosY, color.rgb)
                RenderUtils.newDrawRect(posX, endPosY - 0.5, endPosX, endPosY, color.rgb)
                RenderUtils.newDrawRect(posX - 0.5, posY, endPosX, posY + 0.5, color.rgb)
                RenderUtils.newDrawRect(endPosX - 0.5, posY, endPosX, endPosY, color.rgb)
            } else {
                RenderUtils.newDrawRect(posX + 0.5, posY, posX - 1.0, posY + deltaY / 4.0 + 0.5, Color.BLACK.rgb)
                RenderUtils.newDrawRect(posX - 1.0, endPosY, posX + 0.5, endPosY - deltaY / 4.0 - 0.5, Color.BLACK.rgb)
                RenderUtils.newDrawRect(posX - 1.0, posY - 0.5, posX + deltaX / 3.0 + 0.5, posY + 1.0, Color.BLACK.rgb)
                RenderUtils.newDrawRect(endPosX - deltaX / 3.0 - 0.5, posY - 0.5, endPosX, posY + 1.0, Color.BLACK.rgb)
                RenderUtils.newDrawRect(endPosX - 1.0, posY, endPosX + 0.5, posY + deltaY / 4.0 + 0.5, Color.BLACK.rgb)
                RenderUtils.newDrawRect(endPosX - 1.0, endPosY, endPosX + 0.5, endPosY - deltaY / 4.0 - 0.5, Color.BLACK.rgb)
                RenderUtils.newDrawRect(posX - 1.0, endPosY - 1.0, posX + deltaX / 3.0 + 0.5, endPosY + 0.5, Color.BLACK.rgb)
                RenderUtils.newDrawRect(endPosX - deltaX / 3.0 - 0.5, endPosY - 1.0, endPosX + 0.5, endPosY + 0.5, Color.BLACK.rgb)
                RenderUtils.newDrawRect(posX, posY, posX - 0.5, posY + deltaY / 4.0, color.rgb)
                RenderUtils.newDrawRect(posX, endPosY, posX - 0.5, endPosY - deltaY / 4.0, color.rgb)
                RenderUtils.newDrawRect(posX - 0.5, posY, posX + deltaX / 3.0, posY + 0.5, color.rgb)
                RenderUtils.newDrawRect(endPosX - deltaX / 3.0, posY, endPosX, posY + 0.5, color.rgb)
                RenderUtils.newDrawRect(endPosX - 0.5, posY, endPosX, posY + deltaY / 4.0, color.rgb)
                RenderUtils.newDrawRect(endPosX - 0.5, endPosY, endPosX, endPosY - deltaY / 4.0, color.rgb)
                RenderUtils.newDrawRect(posX, endPosY - 0.5, posX + deltaX / 3.0, endPosY, color.rgb)
                RenderUtils.newDrawRect(endPosX - deltaX / 3.0, endPosY - 0.5, endPosX - 0.5, endPosY, color.rgb)
            }
        }

        val maxHealth = entity.maxHealth
        val health = entity.health.coerceAtMost(maxHealth)

        val progress = health / maxHealth
        val healthTextWidth = deltaY * progress

        if (!healthBarMode.get().equals("none", true)) {
            if (!hoverValue.get() || entity === mc.thePlayer || isHovering(posX, endPosX, posY, endPosY, scaledResolution))
                when (healthNumberMode.get().lowercase()) {
                    "health" -> {
                        val healthDisplay = dFormat.format(health.toDouble()) + " §c❤"
                        drawScaledString(healthDisplay, posX - 4 - Fonts.minecraftFont.getStringWidth(healthDisplay) * fontScaleValue.get(), endPosY - healthTextWidth - Fonts.minecraftFont.FONT_HEIGHT / 2f * fontScaleValue.get(), fontScaleValue.get().toDouble(), -1)
                    }

                    "percent" -> {
                        val healthPercent = (progress * 100f).toInt().toString() + "%"
                        drawScaledString(healthPercent, posX - 4 - Fonts.minecraftFont.getStringWidth(healthPercent) * fontScaleValue.get(), endPosY - healthTextWidth - Fonts.minecraftFont.FONT_HEIGHT / 2f * fontScaleValue.get(), fontScaleValue.get().toDouble(), -1)
                    }
                }

            RenderUtils.newDrawRect(posX - 3.5, posY - 0.5, posX - 1.5, endPosY + 0.5, Color(0, 0, 0, 120).rgb)
            if (health > 0f) {
                val healthColor = BlendUtils.getHealthColor(health, maxHealth).rgb

                when (healthBarMode.get().lowercase()) {
                    "dot" -> if (deltaY >= 60) for (k in 0..9) {
                        val reratio = MathHelper.clamp_double(health - k * maxHealth / 10.0, 0.0, maxHealth / 10.0) / maxHealth * 10.0
                        val hei = (deltaY / 10.0 - 0.5) * reratio
                        RenderUtils.newDrawRect(posX - 3.0, endPosY - (deltaY + 0.5) / 10.0 * k, posX - 2.0, endPosY - (deltaY + 0.5) / 10.0 * k - hei, healthColor)
                     }
                    "line" -> {
                        RenderUtils.newDrawRect(posX - 3.0, endPosY, posX - 2.0, endPosY - healthTextWidth, healthColor)
                        val absorptionAmount = entity.absorptionAmount

                        if (absorption.get() && absorptionAmount > 0f)
                            RenderUtils.newDrawRect(posX - 3.0, endPosY, posX - 2.0, endPosY - deltaY / 6.0 * absorptionAmount / 2.0, Color(Potion.absorption.liquidColor).rgb)
                    }
                }
            }
        }

        when (armorBarMode.get().lowercase()) {
            "items" -> for (index in 4 downTo 1) {
                val armorStack = entity.getEquipmentInSlot(index) ?: continue
                val theHeight = deltaY / 4.0 + 0.25
                val durabilityProgress = MathHelper.clamp_double(ItemUtils.getItemDurability(armorStack) / armorStack.maxDamage.toDouble(), 0.0, 1.0)

                if (armorStack != null && armorStack.item != null) {
                    RenderUtils.newDrawRect(endPosX + 1.5, endPosY + 0.5 - theHeight * index, endPosX + 3.5, endPosY + 0.5 - theHeight * (index - 1), Color(0, 0, 0, 120).rgb)
                    RenderUtils.newDrawRect(endPosX + 2.0, endPosY + 0.5 - theHeight * (index - 1) - 0.25, endPosX + 3.0, endPosY + 0.5 - theHeight * (index - 1) - 0.25 - (deltaY / 4.0 - 0.25) * durabilityProgress, Color(0, 255, 255).rgb)
                }
            }
            "total" -> {
                val armorValue = entity.totalArmorValue.toDouble()
                val armorWidth = deltaY * armorValue / 20.0
                RenderUtils.newDrawRect(endPosX + 1.5, posY - 0.5, endPosX + 3.5, endPosY + 0.5, Color(0, 0, 0, 120).rgb)
                if (armorValue > 0)
                    RenderUtils.newDrawRect(endPosX + 2.0, endPosY, endPosX + 3.0, endPosY - armorWidth, Color(0, 255, 255).rgb)
            }
        }

        if (armorItems.get() && (!hoverValue.get() || entity === mc.thePlayer || isHovering(posX, endPosX, posY, endPosY, scaledResolution))) {
            val yDist = deltaY / 4.0
            for (index in 4 downTo 1) {
                val armorStack = entity.getEquipmentInSlot(index)

                if (armorStack?.item != null) {
                    renderItemStack(armorStack, endPosX + if (!armorBarMode.get().equals("none", true)) 4.0 else 2.0, posY + yDist * (4 - index) + yDist / 2.0 - 5.0)
                    if (armorDurability.get())
                        drawScaledCenteredString(ItemUtils.getItemDurability(armorStack).toString(), endPosX + (if (!armorBarMode.get().equals("none", true)) 4.0 else 2.0) + 4.5, posY + yDist * (4 - index) + yDist / 2.0 + 4.0, fontScaleValue.get().toDouble(), -1)
                }
            }
        }
        if (tagsValue.get()) {
            val entityName = if (clearNameValue.get()) entity.name else entity.displayName.formattedText
            val stringWidth = Fonts.minecraftFont.getStringWidth(entityName)
            if (tagsBGValue.get())
                RenderUtils.newDrawRect(posX + deltaX / 2f - (stringWidth / 2f + 2f) * fontScaleValue.get(), posY - 1f - (Fonts.minecraftFont.FONT_HEIGHT + 2f) * fontScaleValue.get(), posX + deltaX / 2f + (stringWidth / 2f + 2f) * fontScaleValue.get(), posY - 1f + 2f * fontScaleValue.get(), -0x60000000)
            
            drawScaledCenteredString(entityName, posX + deltaX / 2f, posY - 1f - Fonts.minecraftFont.FONT_HEIGHT * fontScaleValue.get(), fontScaleValue.get().toDouble(), -1)
        }

        if (itemTagsValue.get() && entity.heldItem?.item != null) {
            val itemName = entity.heldItem.displayName
            val stringWidth = Fonts.minecraftFont.getStringWidth(itemName)
            if (tagsBGValue.get())
                RenderUtils.newDrawRect(posX + deltaX / 2f - (stringWidth / 2f + 2f) * fontScaleValue.get(), endPosY + 1f - 2f * fontScaleValue.get(), posX + deltaX / 2f + (stringWidth / 2f + 2f) * fontScaleValue.get(), endPosY + 1f + (Fonts.minecraftFont.FONT_HEIGHT + 2f) * fontScaleValue.get(), -0x60000000)
            
            drawScaledCenteredString(itemName, posX + deltaX / 2f, endPosY + 1f, fontScaleValue.get().toDouble(), -1)
        }
    }

    private fun isHovering(minX: Double, maxX: Double, minY: Double, maxY: Double, sc: ScaledResolution): Boolean {
        return sc.scaledWidth / 2 >= minX && sc.scaledWidth / 2 < maxX && sc.scaledHeight / 2 >= minY && sc.scaledHeight / 2 < maxY
    }

    private fun drawScaledString(text: String, x: Double, y: Double, scale: Double, color: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, x)
        GlStateManager.scale(scale, scale, scale)
        if (outlineFont.get()) {
            TTFFontRenderer.drawOutlineStringWithoutGL(text, 0f, 0f, color, mc.fontRendererObj)
        } else {
            Fonts.minecraftFont.drawStringWithShadow(text, 0f, 0f, color)
        }
        GlStateManager.popMatrix()
    }

    private fun drawScaledCenteredString(text: String, x: Double, y: Double, scale: Double, color: Int) {
        drawScaledString(text, x - Fonts.minecraftFont.getStringWidth(text) / 2f * scale, y, scale, color)
    }

    private fun renderItemStack(stack: ItemStack, x: Double, y: Double) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, x)
        GlStateManager.scale(0.5, 0.5, 0.5)
        GlStateManager.enableRescaleNormal()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(stack, 0, 0)
        mc.renderItem.renderItemOverlays(Fonts.minecraftFont, stack, 0, 0)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    private fun project2D(scaleFactor: Int, x: Double, y: Double, z: Double): Vector3d? {
        GL11.glGetFloat(2982, modelview)
        GL11.glGetFloat(2983, projection)
        GL11.glGetInteger(2978, viewport)
        return if (GLU.gluProject(x.toFloat(), y.toFloat(), z.toFloat(), modelview, projection, viewport, vector)) 
            Vector3d((vector[0] / scaleFactor.toFloat()).toDouble(), ((Display.getHeight().toFloat() - vector[1]) / scaleFactor.toFloat()).toDouble(), vector[2].toDouble())
        else null
    }
}