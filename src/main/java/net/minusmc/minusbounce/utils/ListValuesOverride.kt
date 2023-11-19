package net.minusmc.minusbounce.utils

import com.google.gson.JsonElement
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.modules.movement.Speed
import net.minusmc.minusbounce.value.ListValue
import net.minusmc.minusbounce.value.Value

object ListValuesOverride {
    var oldSpeedMode = ""

    fun overrideValue(value: Value<*>, module: Module, element: JsonElement) {
        if (module is Speed && value is ListValue && value.name == "Mode")
            oldSpeedMode = element.asString
    }
}