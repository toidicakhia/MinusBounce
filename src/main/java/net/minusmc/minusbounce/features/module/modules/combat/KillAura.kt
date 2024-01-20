/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.combat

import net.minusmc.minusbounce.MinusBounce
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.*
import net.minecraft.potion.Potion
import net.minecraft.util.*
import net.minecraft.world.WorldSettings
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.features.module.*
import net.minusmc.minusbounce.features.module.modules.exploit.Disabler
import net.minusmc.minusbounce.features.module.modules.movement.TargetStrafe
import net.minusmc.minusbounce.features.module.modules.player.Blink
import net.minusmc.minusbounce.features.module.modules.render.FreeCam
import net.minusmc.minusbounce.features.module.modules.world.Scaffold
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.utils.EntityUtils.isAlive
import net.minusmc.minusbounce.utils.EntityUtils.isSelected
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.utils.timer.*
import net.minusmc.minusbounce.value.*
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.math.*
import net.minusmc.minusbounce.features.module.modules.combat.killaura.KillAuraBlocking

@ModuleInfo(name = "KillAura", spacedName = "Kill Aura", description = "Automatically attacks targets around you.", category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R)
class KillAura : Module() {

    //Blocking modes
    private val blockingModes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.killaura.blocking", KillAuraBlocking::class.java)
        .map { it.newInstance() as KillAuraBlocking }
        .sortedBy { it.modeName }

    private val blockingMode: KillAuraBlocking
        get() = blockingModes.find { autoBlockModeValue.get().equals(it.modeName, true) } ?: throw NullPointerException()
    
    //Options
    private val cps = IntRangeValue("CPS", 5, 8, 1, 20)
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)

    // Range
    val rangeValue = FloatValue("Range", 3.7f, 1f, 8f, "m")
    private val throughWallsValue = BoolValue("ThroughWalls", true)
    private val interactValue = BoolValue("Interact", true)

    // Modes
    private val rotations = ListValue("RotationMode", arrayOf("Vanilla", "BackTrack", "Grim", "Intave", "None"), "BackTrack")
    private val intaveRandomAmount = FloatValue("RandomAmount", 4f, 0.25f, 10f) { rotations.get().equals("Intave", true) }
    private val turnSpeed = FloatRangeValue("TurnSpeed", 180f, 180f, 0f, 180f, "°", {!rotations.get().equals("None", true)})
    private val noHitCheck = BoolValue("NoHitCheck", false) { !rotations.get().equals("none", true) }

    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "HurtResistance", "HurtTime", "Armor"), "Distance")
    val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")
    private val switchDelayValue = IntegerValue("SwitchDelay", 1000, 1, 2000, "ms") {
        targetModeValue.get().equals("switch", true)
    }
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 1, 1, 50) {
        targetModeValue.get().equals("multi", true)
    }

    // Bypass
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")

    public val autoBlockModeValue: ListValue = object : ListValue("AutoBlock", blockingModes.map { it.modeName }.toTypedArray(), "None") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val autoBlockRangeValue = FloatValue("AutoBlock-Range", 5f, 0f, 12f, "m") {
        !autoBlockModeValue.get().equals("None", true)
    }

    // Raycast
    private val raycastValue = BoolValue("RayCast", true)

    private val silentRotationValue = BoolValue("SilentRotation", true) { !rotations.get().equals("none", true) }
    val movementCorrection = BoolValue("MovementCorrection", true)
    // Predict
    private val predictValue = BoolValue("Predict", true)
    private val predictSize = FloatRangeValue("PredictSize", 1f, 1f, 0.1f, 5f) {predictValue.get()}

    // Visuals
    private val circleValue = BoolValue("Circle", true)
    private val accuracyValue = IntegerValue("Accuracy", 59, 0, 59) { circleValue.get() }
    private val red = IntegerValue("Red", 255, 0, 255) { circleValue.get() }
    private val green = IntegerValue("Green", 255, 0, 255) { circleValue.get() }
    private val blue = IntegerValue("Blue", 255, 0, 255) { circleValue.get() }
    private val alpha = IntegerValue("Alpha", 255, 0, 255) { circleValue.get() }

    // Target
    private val prevTargetEntities = mutableListOf<Int>()
    private val discoveredEntities = mutableListOf<EntityLivingBase>()
    var target: EntityLivingBase? = null
    var hitable = false
    
    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0L
    private var clicks = 0

    // Fake block status
    var blockingStatus = false
    
    override fun onDisable() {
        target = null
        hitable = false
        attackTimer.reset()
        clicks = 0
		prevTargetEntities.clear()
        stopBlocking()
        blockingMode.onDisable()
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent) {
        target ?: return

        updateHitable()
        blockingMode.onPostMotion()
    }

    @EventTarget
    fun onEntityMove(event: EntityMovementEvent) {
        val movedEntity = event.movedEntity

        if (target == null || movedEntity != target)
            return

        updateHitable()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        blockingMode.onPacket(event)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (target == null) {
            stopBlocking()
            return
        }

        if (target != null){
            while (clicks > 0) {
                runAttack()
                clicks--
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val targetStrafe = MinusBounce.moduleManager[TargetStrafe::class.java]!!
        if (!targetStrafe.state) return

        if (target != null && RotationUtils.targetRotation != null) {
            if (targetStrafe.canStrafe) {
                val strafingData = targetStrafe.getData()
                MovementUtils.strafe(MovementUtils.speed, strafingData[0], strafingData[1], strafingData[2])
                event.cancelEvent()
            }
        }
    }

    @EventTarget
    fun onPreUpdate(event: PreUpdateEvent){
        updateTarget()
        
        blockingMode.onPreUpdate()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (circleValue.get()) {
            GL11.glPushMatrix()
            GL11.glTranslated(
                mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
                mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY,
                mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
            )
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

            GL11.glLineWidth(1F)
            GL11.glColor4f(red.get().toFloat() / 255.0F, green.get().toFloat() / 255.0F, blue.get().toFloat() / 255.0F, alpha.get().toFloat() / 255.0F)
            GL11.glRotatef(90F, 1F, 0F, 0F)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            for (i in 0..360 step 60 - accuracyValue.get()) { // You can change circle accuracy  (60 - accuracy)
                GL11.glVertex2f(cos(i * Math.PI / 180.0).toFloat() * rangeValue.get(), (sin(i * Math.PI / 180.0).toFloat() * rangeValue.get()))
            }

            GL11.glEnd()

            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)

            GL11.glPopMatrix()
        }

        target ?: return

        if (attackTimer.hasTimePassed(attackDelay) && target!!.hurtTime <= hurtTimeValue.get()) {
            clicks++
            attackTimer.reset()
            attackDelay = TimeUtils.randomClickDelay(cps.getMinValue(), cps.getMaxValue())
        }
    }

    private fun runAttack() {
        target ?: return

        if (!hitable) {
            runSwing()
        } else {
            // Attack
            if (!targetModeValue.get().equals("Multi", true))
                attackEntity(target!!)
            else {
                discoveredEntities
                    .filter {mc.thePlayer.getDistanceToEntityBox(it) < rangeValue.get()}
                    .take(limitedMultiTargetsValue.get())
                    .forEach {attackEntity(it)}
            }

            prevTargetEntities.add(target!!.entityId)
        }

        if (targetModeValue.get().equals("Switch", true)) {
            if (attackTimer.hasTimePassed(switchDelayValue.get().toLong())) {
                prevTargetEntities.add(target!!.entityId)
                attackTimer.reset()
            }
        }
    }

    private fun runSwing() {
        when (swingValue.get().lowercase()) {
            "normal" -> mc.thePlayer.swingItem()
            "packet" -> mc.netHandler.addToSendQueue(C0APacketAnimation())
        }
    }

    private fun updateTarget() {
        discoveredEntities.clear()

        for (entity in mc.theWorld.loadedEntityList) {
			if (entity !is EntityLivingBase || !EntityUtils.isSelected(entity, true) || (targetModeValue.get().equals("switch", true) && prevTargetEntities.contains(entity.entityId)))
                continue

			if (mc.thePlayer.getDistanceToEntityBox(entity) <= rangeValue.get())
				discoveredEntities.add(entity)
		}

        when (priorityValue.get().lowercase()) {
            "distance" -> discoveredEntities.sortBy { mc.thePlayer.getDistanceToEntityBox(it) }
            "health" -> discoveredEntities.sortBy { it.health + it.absorptionAmount }
            "hurtresistance" -> discoveredEntities.sortBy { it.hurtResistantTime }
            "hurttime" -> discoveredEntities.sortBy { it.hurtTime }
            "armor" -> discoveredEntities.sortBy { it.totalArmorValue }
        }

        discoveredEntities.forEach {
			if (updateRotations(it)) {
				target = it
                return
			}
		}

        target = null

        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    private fun attackEntity(entity: EntityLivingBase) {
        val event = AttackEvent(entity)
        MinusBounce.eventManager.callEvent(event)
        if (event.isCancelled) return

        blockingMode.onPreAttack()
        runSwing()

        mc.playerController.syncCurrentPlayItem()
        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))

        if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR)
            mc.thePlayer.attackTargetEntityWithCurrentItem(entity)

        if (interactValue.get()) {
            val (yaw, pitch) = RotationUtils.calculate(getNearestPointBB(mc.thePlayer.getPositionEyes(1F), entity.entityBoundingBox))
            val blockReachDistance: Float = mc.playerController.getBlockReachDistance()
            val vec3: Vec3 = mc.thePlayer.getPositionEyes(1f)
            val vec31: Vec3 = mc.thePlayer.getVectorForRotation(pitch, yaw)
            val vec32: Vec3 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance)
            val mouse: MovingObjectPosition = mc.thePlayer.worldObj.rayTraceBlocks(vec3, vec32, false, false, true)

            if (mouse.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY){
                if(!mc.playerController.isPlayerRightClickingOnEntity(mc.thePlayer, mouse.entityHit, mouse))
                    mc.playerController.interactWithEntitySendPacket(mc.thePlayer, mouse.entityHit)
            } 
        }

        blockingMode.onPostAttack()
    }

    private fun updateRotations(entity: Entity): Boolean {
        if (rotations.get().equals("none", true))
            return true

        val defRotation = getTargetRotation(entity) ?: return false
        
        if (silentRotationValue.get()) {
            RotationUtils.setTargetRot(defRotation, 0)
        } else {
            defRotation.toPlayer(mc.thePlayer!!)
        }
        return true
    }

    private fun getTargetRotation(entity: Entity): Rotation? {
        var boundingBox = entity.entityBoundingBox

        if (predictValue.get() && !rotations.get().equals("Grim", true) && !rotations.get().equals("Intave", true)) {
            boundingBox = boundingBox.offset(
                    (entity.posX - entity.prevPosX) * RandomUtils.nextFloat(predictSize.getMinValue(), predictSize.getMaxValue()),
                    (entity.posY - entity.prevPosY) * RandomUtils.nextFloat(predictSize.getMinValue(), predictSize.getMaxValue()),
                    (entity.posZ - entity.prevPosZ) * RandomUtils.nextFloat(predictSize.getMinValue(), predictSize.getMaxValue())
            )
        }

        val rotationSpeed = (Math.random() * (turnSpeed.getMaxValue() - turnSpeed.getMinValue()) + turnSpeed.getMinValue()).toFloat()
        return when (rotations.get().lowercase()) {
            "vanilla" -> {
                val (_, rotation) = RotationUtils.searchCenter(
                        boundingBox,
                        false,
                        false,
                        predictValue.get(),
                        throughWallsValue.get(),
                        rangeValue.get()
                ) ?: return null

                val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation!!, rotation, rotationSpeed)

                limitedRotation
            }
            "backtrack" -> {
                val rotation = RotationUtils.otherRotation(boundingBox, RotationUtils.getCenter(entity.entityBoundingBox), predictValue.get(), throughWallsValue.get(), rangeValue.get())
                val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation!!, rotation, rotationSpeed)

                limitedRotation
            }
            "grim" -> {
                val rotation = RotationUtils.calculate(getNearestPointBB(mc.thePlayer.getPositionEyes(1F), boundingBox))
                val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation!!, rotation, rotationSpeed)

                limitedRotation
            }
            "intave" -> {
                val rotation: Rotation? = RotationUtils.getAngles(entity)
                val amount = intaveRandomAmount.get()
                val yaw = rotation!!.yaw + Math.random() * amount - amount / 2
                val pitch = rotation.pitch + Math.random() * amount - amount / 2
                val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation!!, Rotation(yaw.toFloat(), pitch.toFloat()), rotationSpeed)

                limitedRotation
            }
            else -> RotationUtils.serverRotation
        }
    }
    private fun updateHitable() {
        if (rotations.get().equals("none", true) || turnSpeed.getMaxValue() <= 0F || noHitCheck.get()) {
            hitable = true
            return
        }

        val reach = min(rangeValue.get().toDouble(), mc.thePlayer.getDistanceToEntityBox(target!!)) + 1

        if (raycastValue.get()) {
            val raycastedEntity = RaycastUtils.raycastEntity(reach, object: RaycastUtils.IEntityFilter {
                override fun canRaycast(entity: Entity?): Boolean {
                    return entity is EntityLivingBase && entity !is EntityArmorStand && isSelected(entity, true)
                }
            })

            if (raycastedEntity is EntityLivingBase)
                target = raycastedEntity

            hitable = if (turnSpeed.getMaxValue() > 0F) target == raycastedEntity else true
        } else
            hitable = RotationUtils.isFaced(target!!, reach)
    }

    fun startBlocking() {
        if (canBlock) {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
            blockingStatus = true
        }
    }

    fun stopBlocking() {
        if (blockingStatus) {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            blockingStatus = false
        }
    }

    val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    override val tag: String
        get() = targetModeValue.get()

    @EventTarget
    fun onPreMotion(event: PreMotionEvent) {
        blockingMode.onPreMotion()
    }
}
