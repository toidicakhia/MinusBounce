package net.minusmc.minusbounce.ui.client.hud.element


class Side(var horizontal: Horizontal, var vertical: Vertical) {

    companion object {
        fun default() = Side(Horizontal.LEFT, Vertical.UP)
    }

    enum class Horizontal(val sideName: String) {
        LEFT("Left"),
        MIDDLE("Middle"),
        RIGHT("Right");

        companion object {
            @JvmStatic
            fun getByName(name: String) = values().find { it.sideName == name }
        }
    }

    enum class Vertical(val sideName: String) {
        UP("Up"),
        MIDDLE("Middle"),
        DOWN("Down");

        companion object {
            @JvmStatic
            fun getByName(name: String) = values().find { it.sideName == name }
        }
    }

}