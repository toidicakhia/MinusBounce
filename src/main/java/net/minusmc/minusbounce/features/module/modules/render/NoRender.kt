/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.render

import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.passive.EntityVillager
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.EntityUtils
import net.minusmc.minusbounce.utils.extensions.getDistanceToEntityBox
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue

@ModuleInfo(name = "NoRender", spacedName = "No Render", description = "Increase FPS by decreasing or stop rendering visible entities.", category = ModuleCategory.RENDER)
class NoRender : Module() {
	private val nameTagsValue = BoolValue("NameTags", true)
    private val itemsValue = BoolValue("Items", true)
    private val playersValue = BoolValue("Players", true)
    private val mobsValue = BoolValue("Mobs", true)
    private val villagerValue = BoolValue("Villager", true)
    private val animalsValue = BoolValue("Animals", true)
    private val armorStandValue = BoolValue("ArmorStand", true)
    private val autoResetValue = BoolValue("AutoReset", true)
    private val maxRenderRange = FloatValue("MaxRenderRange", 4F, 0F, 16F, "m")

    @EventTarget
    fun onRenderNameTags(event: RenderNameTagsEvent) {
        if (nameTagsValue.get()) {
            event.isCancelled = true
            event.stopRunEvent = true
        }
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
    	for (entity in mc.theWorld.loadedEntityList) {
    		if (shouldStopRender(entity))
    			entity.renderDistanceWeight = 0.0
            else if (autoResetValue.get())
                entity.renderDistanceWeight = 1.0
    	}
    }

	fun shouldStopRender(entity: Entity): Boolean {
        mc.thePlayer ?: return false

        if (itemsValue.get() && entity !is EntityItem)
            return false

        if (playersValue.get() && entity !is EntityPlayer)
            return false

        if (villagerValue.get() && entity !is EntityVillager)
            return false

        if (mobsValue.get() && !EntityUtils.isMob(entity))
            return false

        if (animalsValue.get() && !EntityUtils.isAnimal(entity))
            return false

        if (armorStandValue.get() && entity !is EntityArmorStand)
            return false

        return entity != mc.thePlayer && mc.thePlayer.getDistanceToEntityBox(entity) > maxRenderRange.get()
	}

 	override fun onDisable() {
 		for (entity in mc.theWorld.loadedEntityList) {
 			if (entity != mc.thePlayer && entity.renderDistanceWeight <= 0.0)
 				entity.renderDistanceWeight = 1.0
 		}
 	}

}