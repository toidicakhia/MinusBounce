/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.minusmc.minusbounce.features.module.modules.render

import net.minecraft.entity.EntityLivingBase
import net.minusmc.minusbounce.event.AttackEvent
import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.PostMotionEvent
import net.minusmc.minusbounce.event.Render3DEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.particles.EvictingList
import net.minusmc.minusbounce.utils.particles.Particle
import net.minusmc.minusbounce.utils.particles.Vec3
import net.minusmc.minusbounce.utils.render.RenderUtils
import net.minusmc.minusbounce.utils.timer.ParticleTimer
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.IntegerValue

@ModuleInfo(name = "Particles", description = "Particles", category = ModuleCategory.RENDER)
class Particles : Module() {
    private val amount = IntegerValue("Amount", 10, 1, 20)

    private val physics = BoolValue("Physics", true)

    private val particles: MutableList<Particle> = EvictingList(100)
    private val timer: ParticleTimer = ParticleTimer()
    private var target: EntityLivingBase? = null

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) target = event.targetEntity
    }

    @EventTarget
    fun onPostMotion(event: PostMotionEvent?) {
        if (target != null && target!!.hurtTime >= 9 && mc.thePlayer.getDistance(
                target!!.posX,
                target!!.posY,
                target!!.posZ
            ) < 10
        ) {
            for (i in 0 until amount.get()) particles.add(
                Particle(
                    Vec3(
                        target!!.posX + (Math.random() - 0.5) * 0.5,
                        target!!.posY + Math.random() * 1 + 0.5,
                        target!!.posZ + (Math.random() - 0.5) * 0.5
                    )
                )
            )

            target = null
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (particles.isEmpty()) return

        for (i in 0..(timer.elapsedTime / 1E+11).toInt()) {
            if (physics.get()) particles.forEach(Particle::update)
            else particles.forEach(Particle::updateWithoutPhysics)
        }

        particles.removeIf { particle: Particle ->
            mc.thePlayer.getDistanceSq(
                particle.position.xCoord,
                particle.position.yCoord,
                particle.position.zCoord
            ) > 50 * 10
        }

        timer.reset()

        RenderUtils.renderParticles(particles)
    }
}