/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce.features.module.modules.misc

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.UpdateEvent
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.minusbounce.features.module.ModuleCategory
import net.minusmc.minusbounce.features.module.ModuleInfo
import net.minusmc.minusbounce.utils.misc.RandomUtils
import net.minusmc.minusbounce.utils.timer.MSTimer
import net.minusmc.minusbounce.utils.timer.TimeUtils

@ModuleInfo(name = "AutoGroomer", description = "Help you groom people.", category = ModuleCategory.MISC)

class AutoGroomer : Module() {
    private val messages = arrayOf(
            "[MinusBounce] c•a•n• •I• •h•a•v•e• •s•o•m•e• •t•i•t•t•i•e• •p•i•c•s•?",
            "[MinusBounce] d•o• •y•o•u• •w•a•n•n•a• •b•e• •a•b•o•v•e• •o•r• •b•e•l•o•w•?",
            "[MinusBounce] I• •a•m• •g•o•n•n•a• •be• •p•o•u•n•d•i•n•g• •y•o•u• •24/7•",
            "[MinusBounce] I• •a•m• •g•o•n•n•a• •s•e•n•d• •y•o•u• •s•o•m•e•t•h•i•n•g• •o•k•a•y•? •n•o• •s•h•a•r•i•n•g• •:wink:•",
            "[MinusBounce] I• •a•m• •f•i•n•e• •b•e•l•o•w• •o•r• •a•b•o•v•e•",
            "[MinusBounce] y•o•u• •a•r•e• •g•o•n•n•a• •b•e• •r•i•d•i•n•g• •t•h•i•s• •d•i•c•k• •a•l•l• •n•i•g•h•t•",
            "[MinusBounce] o•h• •I• •a•m• •c•r•e•a•m•i•n•g• •j•u•s•t• •l•o•o•k•i•n•g• •a•t• •y•o•u•",
            "[MinusBounce] I• •w•a•n•t• •t•o• •m•a•k•e• •y•o•u• •c•u•m•.",
            "[MinusBounce] m•y• •b•a•l•l•s• •a•r•e• •g•o•n•n•a• •b•e• •d•r•y• •t•o•n•i•g•h•t• •t•h•a•n•k•s• •t•o• •y•o•u",
            "[MinusBounce] I am gonna relieve you all night",
            "[MinusBounce] daddy is ready.",
            "[MinusBounce] you will be screaming my name tonight",
            "[MinusBounce] fly up here and you can have as much as you want",
            "[MinusBounce] lick it off like that, until I ram your mouth.",
            "[MinusBounce] daddy wants your mouth on all of this tonight",
            "[MinusBounce] I bet you like daddy pounding you so hard that your knees give out and drag your face forward as I literally pound you flat into the bed.",
    )

    private val msTimer = MSTimer()
    private var delay = TimeUtils.randomDelay(900, 900)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (msTimer.hasTimePassed(delay)) {
            val indexOfRandomString: Int = RandomUtils.nextInt(0, messages.size)
            mc.thePlayer.sendChatMessage(messages[indexOfRandomString])
            msTimer.reset()
            delay = TimeUtils.randomDelay(900, 900)
        }
    }
}