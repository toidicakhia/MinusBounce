/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.utils.render.shader

import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.MinecraftInstance
import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.*

abstract class Shader(fragmentShader: String) : MinecraftInstance() {
    private var programId: Int = 0
    private var uniformsMap: MutableMap<String, Int>? = null

    init {
        val vertexShaderID: Int
        val fragmentShaderID: Int
        try {
            val vertexStream = javaClass.getResourceAsStream("/assets/minecraft/minusbounce/shader/vertex.vert")
            vertexShaderID = createShader(IOUtils.toString(vertexStream), ARBVertexShader.GL_VERTEX_SHADER_ARB)
            IOUtils.closeQuietly(vertexStream)
            val fragmentStream =
                javaClass.getResourceAsStream("/assets/minecraft/minusbounce/shader/fragment/$fragmentShader")
            fragmentShaderID = createShader(IOUtils.toString(fragmentStream), ARBFragmentShader.GL_FRAGMENT_SHADER_ARB)
            IOUtils.closeQuietly(fragmentStream)

            if (!(vertexShaderID == 0 || fragmentShaderID == 0)) {
                programId = ARBShaderObjects.glCreateProgramObjectARB()
                if (programId != 0) {
                    ARBShaderObjects.glAttachObjectARB(programId, vertexShaderID)
                    ARBShaderObjects.glAttachObjectARB(programId, fragmentShaderID)
                    ARBShaderObjects.glLinkProgramARB(programId)
                    ARBShaderObjects.glValidateProgramARB(programId)
                    ClientUtils.logger.info("[Shader] Successfully loaded: $fragmentShader")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun startShader() {
        GL11.glPushMatrix()
        GL20.glUseProgram(programId)
        if (uniformsMap == null) {
            uniformsMap = HashMap()
            setupUniforms()
        }
        updateUniforms()
    }

    open fun stopShader() {
        GL20.glUseProgram(0)
        GL11.glPopMatrix()
    }

    abstract fun setupUniforms()
    abstract fun updateUniforms()
    private fun createShader(shaderSource: String, shaderType: Int): Int {
        var shader = 0
        return try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType)
            if (shader == 0) return 0
            ARBShaderObjects.glShaderSourceARB(shader, shaderSource)
            ARBShaderObjects.glCompileShaderARB(shader)
            if (ARBShaderObjects.glGetObjectParameteriARB(
                    shader,
                    ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB
                ) == GL11.GL_FALSE
            ) throw RuntimeException("Error creating shader: " + getLogInfo(shader))
            shader
        } catch (e: Exception) {
            ARBShaderObjects.glDeleteObjectARB(shader)
            throw e
        }
    }

    private fun getLogInfo(i: Int): String {
        return ARBShaderObjects.glGetInfoLogARB(
            i,
            ARBShaderObjects.glGetObjectParameteriARB(i, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB)
        )
    }

    private fun setUniform(uniformName: String, location: Int) {
        uniformsMap!![uniformName] = location
    }

    fun setupUniform(uniformName: String) {
        setUniform(uniformName, GL20.glGetUniformLocation(programId, uniformName))
    }

    fun getUniform(uniformName: String): Int {
        return uniformsMap!![uniformName]!!
    }
}
