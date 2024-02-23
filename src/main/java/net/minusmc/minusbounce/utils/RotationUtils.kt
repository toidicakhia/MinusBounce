/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityEgg
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.*
import net.minusmc.minusbounce.event.*
import net.minusmc.minusbounce.utils.extensions.*
import net.minusmc.minusbounce.utils.misc.*
import java.util.*
import kotlin.math.*


object RotationUtils : MinecraftInstance(), Listenable {
    private val random = Random()
    private var keepLength = 0

    @JvmField
    var targetRotation: Rotation? = null

    @JvmField
    var serverRotation = Rotation(0f, 0f)

    private var x = random.nextDouble()
    private var y = random.nextDouble()
    private var z = random.nextDouble()
    
    @EventTarget
    fun onTick(event: TickEvent) {
        if (targetRotation != null){
            keepLength--

            if (keepLength <= 0){
                targetRotation = null
                keepLength = 0
            }
        }

        if (random.nextGaussian() > 0.8) x = Math.random()
        if (random.nextGaussian() > 0.8) y = Math.random()
        if (random.nextGaussian() > 0.8) z = Math.random()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            val packetPlayer = packet
            if (targetRotation != null && (targetRotation!!.yaw != serverRotation.yaw || targetRotation!!.pitch != serverRotation.pitch)) {
                packetPlayer.yaw = targetRotation!!.yaw
                packetPlayer.pitch = targetRotation!!.pitch
                packetPlayer.rotating = true
            }
            if (packetPlayer.rotating) serverRotation = Rotation(packetPlayer.yaw, packetPlayer.pitch)
        }
    }

    fun setTargetRot(rotation: Rotation, keepLength: Int) {
        if (rotation.yaw.isNaN() || rotation.pitch.isNaN() || rotation.pitch > 90 || rotation.pitch < -90)
            return
        rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)
        targetRotation = rotation
        this.keepLength = keepLength
    }

    fun setTargetRot(rotation: Rotation) {
        setTargetRot(rotation, 0)
    }

    /**
     * Face block
     *
     * @param blockPos target block
     */
    fun faceBlock(blockPos: BlockPos?): VecRotation? {
        if (blockPos == null) return null
        var vecRotation: VecRotation? = null

        for (xSearch in 0.1..0.9) {
            for (ySearch in 0.1..0.9) {
                for (zSearch in 0.1..0.9) {

                    val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ)
                    val posVec = Vec3(blockPos).addVector(xSearch, ySearch, zSearch)
                    val dist = eyesPos.distanceTo(posVec)
                    val diffX = posVec.xCoord - eyesPos.xCoord
                    val diffY = posVec.yCoord - eyesPos.yCoord
                    val diffZ = posVec.zCoord - eyesPos.zCoord
                    val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
                    val rotation = Rotation(
                        MathUtils.wrapAngleTo180(MathUtils.toDegrees(atan2(diffZ, diffX)) - 90),
                        MathUtils.wrapAngleTo180(-MathUtils.toDegrees(atan2(diffY, diffXZ)))
                    )
                    val rotationVector = getVectorForRotation(rotation).multiply(dist)
                    val vector = eyesPos.add(rotationVector)
                    val obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true)
                    if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        val currentVec = VecRotation(posVec, rotation)
                        if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation))
                            vecRotation = currentVec
                    }

                }
            }
        }

        return vecRotation
    }

    /**
     * Face target with bow
     *
     * @param target your enemy
     * @param silent client side rotations
     * @param predict predict new enemy position
     * @param predictSize predict size of predict
     */
    fun faceBow(target: Entity, silent: Boolean, predict: Boolean, predictSize: Float) {
        val diffX = target.posX - mc.thePlayer.posX + if (predict) (target.posX - target.prevPosX) * predictSize - (mc.thePlayer.posX - mc.thePlayer.prevPosX) * predictSize else 0.0
        val diffY = target.entityBoundingBox.minY + target.eyeHeight - 0.15 - mc.thePlayer.entityBoundingBox.minY - mc.thePlayer.eyeHeight + if (predict) (target.entityBoundingBox.minY - target.prevPosY) * predictSize - (mc.thePlayer.posY - mc.thePlayer.prevPosY) * predictSize else 0.0
        val diffZ = target.posX - mc.thePlayer.posZ + if (predict) (target.posZ - target.prevPosZ) * predictSize - (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * predictSize else 0.0

        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)

        var velocity = mc.thePlayer.itemInUseDuration / 20f
        velocity = (velocity * velocity + velocity * 2) / 3

        if (velocity > 1)
            velocity = 1f

        val diffVelocity = (velocity * velocity - sqrt(velocity * velocity * velocity * velocity - 0.006f * (0.006f * diffXZ * diffXZ + 2 * diffY * velocity * velocity))) / (0.006f * diffXZ)

        val rotation = Rotation(
            MathUtils.toDegrees(atan2(diffZ, diffX)) - 90,
            -MathUtils.toDegrees(atan(diffVelocity))
        )
        if (silent) 
            setTargetRot(rotation)
        else limitAngleChange(mc.thePlayer.rotation, rotation, RandomUtils.nextFloat(10f, 16f)).toPlayer(mc.thePlayer)
    }

    /**
     * Translate vec to rotation
     *
     * @param vec target vec
     * @param predict predict new location of your body
     * @return rotation
     */
    fun toRotation(vec: Vec3, predict: Boolean): Rotation {
        val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ)
        if (predict)
            eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)
        val diffX = vec.xCoord - eyesPos.xCoord
        val diffY = vec.yCoord - eyesPos.yCoord
        val diffZ = vec.zCoord - eyesPos.zCoord

        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)

        return Rotation(
            MathUtils.wrapAngleTo180(MathUtils.toDegrees(atan2(diffZ, diffX)) - 90f),
            MathUtils.wrapAngleTo180(-MathUtils.toDegrees(atan2(diffY, diffXZ)))
        )
    }

    /**
     * Get the center of a box
     *
     * @param bb your box
     * @return center of box
     */
    fun getCenter(bb: AxisAlignedBB) = Vec3(bb.minX + (bb.maxX - bb.minX) * 0.5, bb.minY + (bb.maxY - bb.minY) * 0.5, bb.minZ + (bb.maxZ - bb.minZ) * 0.5)

    /**
     * Round rotation
     */
    fun roundRotation(yaw: Float, strength: Int): Float {
        return round(yaw / strength) * strength
    }

    /**
     * Search good center
     */
    fun searchCenter(bb: AxisAlignedBB, predict: Boolean, throughWalls: Boolean, distance: Float): VecRotation? {
        val eyes = mc.thePlayer.getPositionEyes(1f)
        var vecRotation: VecRotation? = null

        for (xSearch in 0.15..0.85) {
            for (ySearch in 0.15..0.85) {
                for (zSearch in 0.15..0.85) {
                    val vec3 = Vec3(
                        bb.minX + (bb.maxX - bb.minX) * xSearch,
                        bb.minY + (bb.maxY - bb.minY) * ySearch,
                        bb.minZ + (bb.maxZ - bb.minZ) * zSearch
                    )
                    val rotation = toRotation(vec3, predict)
                    val vecDist = eyes.distanceTo(vec3)
                    if (vecDist > distance) continue
                    if (throughWalls || isVisible(vec3)) {
                        val currentVec = VecRotation(vec3, rotation)
                        if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation))
                            vecRotation = currentVec
                    }
                }
            }
        }

        return vecRotation
    }

    /**
     * Calculate difference between the client rotation and your entity
     */
    fun getRotationDifference(entity: Entity): Double {
        val rotation = toRotation(getCenter(entity.entityBoundingBox), true)
        return getRotationDifference(rotation, Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch))
    }

    /**
     * Calculate difference between the client rotation and your entity's back
     */
    fun getRotationBackDifference(entity: Entity): Double {
        val rotation = toRotation(getCenter(entity.entityBoundingBox), true)
        return getRotationDifference(rotation, Rotation(mc.thePlayer.rotationYaw - 180, mc.thePlayer.rotationPitch))
    }

    /**
     * Calculate difference between the server rotation and your rotation
     */
    fun getRotationDifference(rotation: Rotation): Double {
        return getRotationDifference(rotation, serverRotation)
    }

    /**
     * Calculate difference between two rotations
     */
    fun getRotationDifference(a: Rotation, b: Rotation?): Double {
        return hypot(getAngleDifference(a.yaw, b!!.yaw).toDouble(), (a.pitch - b.pitch).toDouble())
    }

    /**
     * Limit your rotation using a turn speed
     */
    fun limitAngleChange(currentRotation: Rotation, targetRotation: Rotation, turnSpeed: Float): Rotation {
        val yawDifference = getAngleDifference(targetRotation.yaw, currentRotation.yaw)
        val pitchDifference = getAngleDifference(targetRotation.pitch, currentRotation.pitch)
        return Rotation(
            currentRotation.yaw + if (yawDifference > turnSpeed) turnSpeed else max(yawDifference, -turnSpeed),
            currentRotation.pitch + if (pitchDifference > turnSpeed) turnSpeed else max(pitchDifference, -turnSpeed)
        )
    }

    /**
     * Calculate difference between two angle points
     */
    fun getAngleDifference(a: Float, b: Float): Float {
        return ((a - b) % 360f + 540f) % 360f - 180f
    }

    /**
     * Calculate rotation to vector
     */
    fun getVectorForRotation(rotation: Rotation): Vec3 {
        val yawCos = cos(-rotation.yaw * 0.017453292f - Math.PI.toFloat())
        val yawSin = sin(-rotation.yaw * 0.017453292f - Math.PI.toFloat())
        val pitchCos = -cos(-rotation.pitch * 0.017453292f)
        val pitchSin = sin(-rotation.pitch * 0.017453292f)
        return Vec3((yawSin * pitchCos).toDouble(), pitchSin.toDouble(), (yawCos * pitchCos).toDouble())
    }

    /**
     * Allows you to check if your crosshair is over your target entity
     */
    fun isFaced(targetEntity: Entity, blockReachDistance: Double): Boolean {
        return RaycastUtils.raycastEntity(blockReachDistance,
            object : RaycastUtils.IEntityFilter {
                override fun canRaycast(entity: Entity?) = entity === targetEntity
            }) != null
    }

    /**
     * Allows you to check if your enemy is behind a wall
     */
    fun isVisible(vec3: Vec3?): Boolean {
        val eyesPos = Vec3(
            mc.thePlayer.posX,
            mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight,
            mc.thePlayer.posZ
        )
        return mc.theWorld.rayTraceBlocks(eyesPos, vec3) == null
    }

    fun getRotationsEntity(entity: EntityLivingBase): Rotation {
        return getRotations(entity.posX, entity.posY + entity.eyeHeight - 0.4, entity.posZ)
    }

    fun getRotations(ent: Entity): Rotation {
        val x = ent.posX
        val y = ent.posY + ent.eyeHeight.toDouble() / 2.0
        val z = ent.posZ
        return getRotationFromPosition(x, z, y)
    }

    fun getRotations(posX: Double, posY: Double, posZ: Double): Rotation {
        val x = posX - mc.thePlayer.posX
        val y = posY - mc.thePlayer.posY - mc.thePlayer.eyeHeight.toDouble()
        val z = posZ - mc.thePlayer.posZ

        val dist = sqrt(x * x + z * z)

        return Rotation(
            MathUtils.toDegrees(atan2(z, x)) - 90.0f, 
            -MathUtils.toDegrees(atan2(y, dist))
        )
    }

    fun getRotationFromPosition(x: Double, z: Double, y: Double): Rotation {
        val xDiff = x - mc.thePlayer.posX
        val yDiff = y - mc.thePlayer.posY - 1.2
        val zDiff = z - mc.thePlayer.posZ

        val xzDiff = sqrt(xDiff * xDiff + zDiff * zDiff)

        return Rotation(
            MathUtils.toDegrees(atan2(z, x)) - 90.0f, 
            -MathUtils.toDegrees(atan2(y, xzDiff))
        )
    }

    fun calculate(from: Vec3?, to: Vec3): Rotation {
        val diff = to.subtract(from)
        val distance = hypot(diff.xCoord, diff.zCoord)
        val yaw = MathUtils.toDegrees(atan2(diff.zCoord, diff.xCoord)) - 90.0
        val pitch = -MathUtils.toDegrees(atan2(diff.yCoord, distance))
        return Rotation(yaw, pitch)
    }

    fun calculate(to: Vec3): Rotation {
        return calculate(mc.thePlayer.positionVector.addVector(0.0, mc.thePlayer.eyeHeight.toDouble(), 0.0), to)
    }

    fun getAngles(entity: Entity): Rotation {
        val diffX = entity.posX - mc.thePlayer.posX
        val diffY = entity.posY + entity.eyeHeight * 0.9 - (mc.thePlayer.posY + mc.thePlayer.eyeHeight)
        val diffZ = entity.posZ - mc.thePlayer.posZ
        val dist = sqrt(diffX * diffX + diffZ * diffZ)
        val yaw = MathUtils.toDegrees(atan2(z, x)) - 90.0f
        val pitch = -MathUtils.toDegrees(atan2(y, dist))
        return Rotation(
            mc.thePlayer.rotationYaw + MathUtils.wrapAngleTo180(yaw - mc.thePlayer.rotationYaw),
            mc.thePlayer.rotationPitch + MathUtils.wrapAngleTo180(pitch - mc.thePlayer.rotationPitch)
        )
    }

    fun getDirectionToBlock(x: Double, y: Double, z: Double, enumfacing: EnumFacing): Rotation {
        val entity = EntityEgg(mc.theWorld)
        entity.posX = x + 0.5
        entity.posY = y + 0.5
        entity.posZ = z + 0.5
        entity.posX += enumfacing.directionVec.x.toDouble() * 0.5
        entity.posY += enumfacing.directionVec.y.toDouble() * 0.5
        entity.posZ += enumfacing.directionVec.z.toDouble() * 0.5
        return getRotations(entity.posX, entity.posY, entity.posZ)
    }

    override fun handleEvents() = true
}