package su.funmoon.fmbungee

import net.md_5.bungee.api.plugin.Plugin
import su.funmoon.fmbungee.config.ConfigManager
import su.funmoon.fmbungee.feature.admin.command.FmBungeeCommand
import su.funmoon.fmbungee.feature.ping.command.PingCommand

class FmBungee : Plugin() {

    lateinit var configManager: ConfigManager
        private set

    companion object {
        @JvmStatic
        lateinit var instance: FmBungee
            private set
    }

    override fun onEnable() {
        instance = this

        configManager = ConfigManager(this)
        configManager.loadConfig()

        proxy.pluginManager.registerCommand(this, FmBungeeCommand(this))
        proxy.pluginManager.registerCommand(this, PingCommand(this))

        logger.info("FmBungee успешно запущен!")
    }

    override fun onDisable() {
        logger.info("FmBungee выключен.")
    }
}
