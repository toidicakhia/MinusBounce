package net.minusmc.minusbounce.ui.client.hud.element

import net.minusmc.minusbounce.utils.render.RenderUtils

data class Border(val x: Float, val y: Float, val x2: Float, val y2: Float, val color: Int) {
    constructor(x: Float, y: Float, x2: Float, y2: Float): this(x, y, x2, y2, Int.MIN_VALUE)
    fun draw() = RenderUtils.drawBorderedRect(x, y, x2, y2, 1F, color, 0) // hihi fixed amazing
}
