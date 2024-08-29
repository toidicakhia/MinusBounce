package net.minusmc.minusbounce.features.module.modules.render.esps

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.modules.render.ESP
import net.minusmc.minusbounce.utils.ClassUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.minusmc.minusbounce.value.Value
import net.minecraft.entity.EntityLivingBase
import java.awt.Color


abstract class ESPMode(val modeName: String): MinecraftInstance() {
	protected val esp: ESP
		get() = MinusBounce.moduleManager[ESP::class.java]!!

	open val values: List<Value<*>>
		get() = ClassUtils.getValues(this.javaClass, this)

	open fun onEnable() {}
	open fun onDisable() {}
	open fun onPreRender3D() {}
	open fun onPostRender3D() {}
	open fun onRenderNameTags(event: RenderNameTagsEvent) {}
	open fun onRender2D(event: Render2DEvent, color: Color) {}
	open fun onEntityRender(entity: EntityLivingBase, color: Color) {}
	open fun onRenderModel(event: RenderModelEvent, color: Color) {}
}
