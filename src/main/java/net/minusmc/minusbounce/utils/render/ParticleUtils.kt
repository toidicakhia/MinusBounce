/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render

import net.minecraft.entity.projectile.EntityEgg
import net.minusmc.minusbounce.utils.MinecraftInstance
import net.vitox.ParticleGenerator
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object ParticleUtils: MinecraftInstance() {
    private val particleGenerator = ParticleGenerator(100)
    
    fun drawParticles(mouseX: Int, mouseY: Int) {
        particleGenerator.draw(mouseX, mouseY)
    }

    fun drawSnowFall(mouseX: Int, mouseY: Int) {
        particleGenerator.draw2(mouseX, mouseY)
    }

    fun renderParticles(particles: List<Particle>) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_LINE_SMOOTH)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA) 

        particles.forEachIndexed {i, particle ->
            val pos = particle.position
            val x = pos.xCoord - mc.renderManager.renderPosX
            val y = pos.yCoord - mc.renderManager.renderPosY
            val z = pos.zCoord - mc.renderManager.renderPosZ

            val distance = mc.thePlayer.getDistance(pos.xCoord, pos.yCoord - 1, pos.zCoord)
            var quality = (distance * 4 + 10).toInt().coerceAtMost(350)

            if (!RenderUtils.isInViewFrustrum(EntityEgg(mc.theWorld, pos.xCoord, pos.yCoord, pos.zCoord)) || (i + 1) % 10 != 0 && distance > 25 || (i + 1) % 3 == 0 && distance > 15)
                return@forEachIndexed

            glPushMatrix()
            glTranslated(x, y, z)

            glScalef(-0.04f, -0.04f, -0.04f)
            glRotated(-mc.renderManager.playerViewY.toDouble(), 0.0, 1.0, 0.0)
            glRotated(mc.renderManager.playerViewX.toDouble(), if (mc.gameSettings.thirdPersonView == 2) -1.0 else 1.0, 0.0, 0.0)
            
            val color = Color(ColorUtils.getHSBColor(-1.7f * 5f - 1f, 0.7f, 1f))
            RenderUtils.drawFilledCircleNoGL(0, 0, 0.7, color, quality)
            
            if (distance < 4)
                RenderUtils.drawFilledCircleNoGL(0, 0, 1.4, Color(color.red, color.green, color.blue, 50), quality)
            
            if (distance < 20) 
                RenderUtils.drawFilledCircleNoGL(0, 0, 2.3, Color(color.red, color.green, color.blue, 30), quality)
            
            glScalef(0.8f, 0.8f, 0.8f)
            glPopMatrix()
        }

        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glColor3f(255f, 255f, 255f)
    }
}
