package net.minusmc.minusbounce.utils.render

import net.minusmc.minusbounce.utils.block.BlockUtils
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minecraft.util.Vec3
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockBush
import net.minecraft.block.BlockLiquid

class Particle(x: Double, y: Double, z: Double) {

    val position = Vec3(
        x + RandomUtils.nextDouble() * 0.5 - 0.25, 
        y + RandomUtils.nextDouble() + 0.5, 
        z + RandomUtils.nextDouble() * 0.5 - 0.25)

    private val delta = Vec3(
        RandomUtils.nextDouble() * 0.025 - 0.0125, 
        RandomUtils.nextDouble() * 0.005 - 0.002,
        RandomUtils.nextDouble() * 0.025 - 0.0125
    )

    fun update() {
        val blockX = BlockUtils.getBlock(position.xCoord, position.yCoord, position.zCoord + delta.zCoord)
        if (blockX !is BlockAir && blockX !is BlockBush && blockX !is BlockLiquid)
            delta.zCoord *= -0.8

        val blockY = BlockUtils.getBlock(position.xCoord, position.yCoord + delta.yCoord, position.zCoord)
        if (blockY !is BlockAir && blockY !is BlockBush && blockY !is BlockLiquid) {
            delta.xCoord *= 0.999
            delta.zCoord *= 0.999
            delta.yCoord *= -0.6
        }

        val blockZ = BlockUtils.getBlock(position.xCoord + delta.xCoord, position.yCoord, position.zCoord)
        if (blockZ !is BlockAir && blockZ !is BlockBush && blockZ !is BlockLiquid)
            delta.xCoord *= -0.8

        updateWithoutPhysics()
    }

    fun updateWithoutPhysics() {
        position.xCoord += delta.xCoord
        position.yCoord += delta.yCoord
        position.zCoord += delta.zCoord
        delta.xCoord /= 0.999997
        delta.yCoord -= 0.0000017
        delta.zCoord /= 0.999997
    }
}