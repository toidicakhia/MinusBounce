package net.minusmc.minusbounce.features.module.modules.combat.velocitys.grim

import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minusmc.minusbounce.event.PacketEvent
import net.minusmc.minusbounce.event.TickEvent
import net.minusmc.minusbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue

class GrimC07Velocity : VelocityMode("GrimC07") {
    private val alwaysValue = BoolValue("Always", true)
    private val onlyAirValue = BoolValue("OnlyBreakAir", true)
    private val worldValue = BoolValue("BreakOnWorld", false)
    private val sendC03Value = BoolValue("SendC03", false)
    private val c06Value = BoolValue("Send1.17C06", false) { sendC03Value.get() }
    private val flagPauseValue = IntegerValue("FlagPause-Time", 50, 0, 5000)

    private var gotVelo = false
    private var flagTimer = MSTimer()

    override fun onEnable() {
        gotVelo = false
        flagTimer.reset()
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook)
            flagTimer.reset()
        if (!flagTimer.hasTimePassed(flagPauseValue.get().toLong())) {
            gotVelo = false
            return
        }

        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer?.entityId) {
            event.cancelEvent()
            gotVelo = true
        } else if (packet is S27PacketExplosion) {
            event.cancelEvent()
            gotVelo = true
        }
    }

    override fun onTick(event: TickEvent) {

        if (!flagTimer.hasTimePassed(flagPauseValue.get().toLong())) {
            gotVelo = false
            return
        }

        val thePlayer = mc.thePlayer ?: return
        val theWorld = mc.theWorld ?: return
        if (gotVelo || alwaysValue.get()) { // packet processed event pls
            val pos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
            if (checkBlock(pos, theWorld) || checkBlock(pos.up(), theWorld)) {
                if (sendC03Value.get()) {
                    if (c06Value.get())
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C06PacketPlayerPosLook(thePlayer.posX, thePlayer.posY, thePlayer.posZ, thePlayer.rotationYaw, thePlayer.rotationPitch, thePlayer.onGround))
                    else
                        mc.netHandler.addToSendQueue(C03PacketPlayer(thePlayer.onGround))
                }
            }
            gotVelo = false
        }
    }

    private fun checkBlock(pos: BlockPos, theWorld: World): Boolean {
        if (!onlyAirValue.get() || theWorld.isAirBlock(pos)) {
            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.DOWN))
            if (worldValue.get())
                theWorld.setBlockToAir(pos)
            return true
        }
        return false
    }
}
