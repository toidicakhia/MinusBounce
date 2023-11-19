/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.ui.client.clickgui.elements

import net.minusmc.minusbounce.MinusBounce

open class ButtonElement(displayName: String?) : Element() {
    var displayName: String? = null
        protected set
    var color = 0xffffff
    var hoverTime = 0

    init {
        createButton(displayName)
    }

    private fun createButton(displayName: String?) {
        this.displayName = displayName
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, button: Float) {
        MinusBounce.clickGui.style.drawButtonElement(mouseX, mouseY, this)
        super.drawScreen(mouseX, mouseY, button)
    }

    override var height: Int
        get() = 16
        set(height) {
            super.height = height
        }

    fun isHovering(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16
    }
}
