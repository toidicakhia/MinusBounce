/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.client

import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.value.BoolValue
import net.minusmc.minusbounce.value.FloatValue
import net.minusmc.minusbounce.value.IntegerValue
import net.minusmc.minusbounce.value.ListValue


@ModuleInfo(name = "Animations", description = "Render items, animations.", category = ModuleCategory.CLIENT)
object Animations : Module() {
    val Sword: ListValue = ListValue(
        "Style", arrayOf(
            "Normal",
            "Slidedown",
            "Slidedown2",
            "Slide",
            "Minecraft",
            "Remix",
            "Exhibition",
            "Exhibition2",
            "Avatar",
            "Swang",
            "Tap",
            "Tap2",
            "Poke",
            "Push",
            "Push2",
            "Up",
            "Shield",
            "Akrien",
            "VisionFX",
            "Swong",
            "Swank",
            "SigmaOld",
            "ETB",
            "Rotate360",
            "SmoothFloat",
            "Strange",
            "Reverse",
            "Zoom",
            "Move",
            "Stab",
            "Jello",
            "1.7",
            "Flux",
            "Stella",
            "Tifality",
            "OldExhibition"
        ), "Minecraft"
    )
    // item general scale
    val Scale = FloatValue("Scale", 0.4f, 0f, 4f)

    // normal item position
    val itemPosX = FloatValue("ItemX", 0f, -1f, 1f)
    val itemPosY = FloatValue("ItemY", 0f, -1f, 1f)
    val itemPosZ = FloatValue("ItemZ", 0f, -1f, 1f)

    // change Position Blocking Sword
    val blockPosX = FloatValue("BlockingX", 0f, -1f, 1f)
    val blockPosY = FloatValue("BlockingY", 0f, -1f, 1f)
    val blockPosZ = FloatValue("BlockingZ", 0f, -1f, 1f)

    // modify item swing and rotate
    val SpeedSwing = IntegerValue("Swing-Speed", 4, 0, 20)
    val RotateItems = BoolValue("Rotate-Items", false)
    val SpeedRotate = FloatValue("Rotate-Speed", 1f, 0f, 10f) { RotateItems.get() }

    // transform rotation
    val transformFirstPersonRotate = ListValue("RotateMode", arrayOf("RotateY", "RotateXY", "Custom", "None"), "RotateY")

    // custom item rotate
    val customRotate1 = FloatValue("RotateXAxis", 0f, -180f, 180f) {
        RotateItems.get() && transformFirstPersonRotate.get().equals("custom", true)
    }
    val customRotate2 = FloatValue("RotateYAxis", 0f, -180f, 180f) {
        RotateItems.get() && transformFirstPersonRotate.get().equals("custom", true)
    }
    val customRotate3 = FloatValue("RotateZAxis", 0f, -180f, 180f) {
        RotateItems.get() && transformFirstPersonRotate.get().equals("custom", true)
    }

    // gui animations
    val guiAnimations = ListValue("Container-Animation", arrayOf("None", "Zoom", "Slide", "Smooth"), "None")
    val vSlideValue = ListValue("Slide-Vertical", arrayOf("None", "Upward", "Downward"), "Downward") {
        guiAnimations.get().equals("slide", true)
    }
    val hSlideValue = ListValue("Slide-Horizontal", arrayOf("None", "Right", "Left"), "Right") {
        guiAnimations.get().equals("slide", true)
    }
    val animTimeValue = IntegerValue("Container-AnimTime", 750, 0, 3000) {
        !guiAnimations.get().equals("none", true)
    }
    val tabAnimations = ListValue("Tab-Animation", arrayOf("None", "Zoom", "Slide"), "Zoom")

    // custom animation sword
    val mcSwordPos: FloatValue = FloatValue("MCPosOffset", 0.45f, 0f, 0.5f) {
        Sword.get().equals("minecraft", ignoreCase = true)
    }

    val fakeBlocking = BoolValue("Fake-Blocking", true)

    // block crack
    val noBlockParticles = BoolValue("NoBlockParticles", false)
}
