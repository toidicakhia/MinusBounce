/*
 * MinusBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MinusMC/MinusBounce
 */
package net.minusmc.minusbounce

import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import net.minusmc.minusbounce.event.ClientShutdownEvent
import net.minusmc.minusbounce.event.EventManager
import net.minusmc.minusbounce.features.command.CommandManager
import net.minusmc.minusbounce.features.module.ModuleManager
import net.minusmc.minusbounce.features.special.AntiForge
import net.minusmc.minusbounce.features.special.BungeeCordSpoof
import net.minusmc.minusbounce.features.special.CombatManager
import net.minusmc.minusbounce.features.special.MacroManager
import net.minusmc.minusbounce.features.special.SessionManager
import net.minusmc.minusbounce.utils.player.MovementCorrection
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.file.FileManager
import net.minusmc.minusbounce.plugin.PluginAPIVersion
import net.minusmc.minusbounce.plugin.PluginManager
import net.minusmc.minusbounce.ui.client.altmanager.GuiAltManager
import net.minusmc.minusbounce.ui.client.clickgui.dropdown.DropDownClickGui
import net.minusmc.minusbounce.ui.client.hud.HUD
import net.minusmc.minusbounce.ui.client.hud.HUD.Companion.createDefault
import net.minusmc.minusbounce.utils.*
import net.minusmc.minusbounce.utils.misc.sound.TipSoundManager
import net.minusmc.minusbounce.utils.player.RotationUtils
import net.minusmc.minusbounce.utils.render.RenderUtils

object MinusBounce {

    // Client information
    const val CLIENT_NAME = "MinusBounce"
    const val CLIENT_FOLDER = "MinusBounce"
    const val CLIENT_VERSION = "dev"
    const val CLIENT_CREATOR = "CCBlueX, MinusMC"
    val API_VERSION = PluginAPIVersion.VER_01
    const val CLIENT_CLOUD = "https://minusmc.github.io/MinusCloud/LiquidBounce"
    
    var isStarting = false

    // Managers
    lateinit var moduleManager: ModuleManager
    lateinit var commandManager: CommandManager
    lateinit var eventManager: EventManager
    lateinit var combatManager: CombatManager
    lateinit var fileManager: FileManager
    lateinit var tipSoundManager: TipSoundManager
    lateinit var pluginManager: PluginManager
    lateinit var clickGui: DropDownClickGui
    lateinit var sessionManager: SessionManager

    // HUD & ClickGUI
    lateinit var hud: HUD

    // Menu Background
    var background: ResourceLocation? = null

    private var lastTick : Long = 0L

    fun startClient() {
        isStarting = true

        ClientUtils.logger.info("Starting $CLIENT_NAME")
        ClassUtils.initCacheClass()
        lastTick = System.currentTimeMillis()

        fileManager = FileManager()
        eventManager = EventManager()
        combatManager = CombatManager()
        sessionManager = SessionManager()
        eventManager.registerListener(RotationUtils)
        eventManager.registerListener(MovementCorrection)
        eventManager.registerListener(AntiForge())
        eventManager.registerListener(BungeeCordSpoof())
        eventManager.registerListener(InventoryUtils)
        eventManager.registerListener(PacketUtils)
        eventManager.registerListener(SessionUtils)
        eventManager.registerListener(MacroManager)
        eventManager.registerListener(combatManager)
        eventManager.registerListener(sessionManager)

        commandManager = CommandManager()
        tipSoundManager = TipSoundManager()

        Fonts.loadFonts()

        moduleManager = ModuleManager()
        moduleManager.registerModules()
        // plugin load modules   

        pluginManager = PluginManager()
        pluginManager.registerPlugins()
        pluginManager.initPlugins()

        pluginManager.registerModules()

        commandManager.registerCommands()
        pluginManager.registerCommands()
        // plugin load command

        fileManager.loadConfigs(fileManager.modulesConfig, fileManager.valuesConfig, fileManager.accountsConfig, fileManager.friendsConfig)

        clickGui = DropDownClickGui()
        fileManager.loadConfig(fileManager.clickGuiConfig)

        moduleManager.initModeListValues()

        // Set HUD
        hud = createDefault()
        fileManager.loadConfig(fileManager.hudConfig)
        ClientUtils.logger.info("Finished loading $CLIENT_NAME in ${System.currentTimeMillis() - lastTick}ms.")

        // Set is starting status
        isStarting = false
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        eventManager.callEvent(ClientShutdownEvent())

        // Save all available configs
        fileManager.saveAllConfigs()
    }

}