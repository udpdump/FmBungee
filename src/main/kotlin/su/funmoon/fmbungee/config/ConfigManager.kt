package su.funmoon.fmbungee.config

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import su.funmoon.fmbungee.FmBungee
import su.funmoon.fmbungee.utils.TextUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.logging.Level

class ConfigManager(private val plugin: FmBungee) {

    var configuration: Configuration? = null
        private set

    private val configFile: File = File(plugin.dataFolder, "config.yml")

    fun loadConfig() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdir()
        }

        if (!configFile.exists()) {
            try {
                plugin.getResourceAsStream("config.yml")?.use { input ->
                    Files.copy(input, configFile.toPath())
                } ?: configFile.createNewFile()
            } catch (e: IOException) {
                plugin.logger.log(Level.SEVERE, "Не удалось создать config.yml", e)
            }
        }

        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(configFile)
        } catch (e: IOException) {
            plugin.logger.log(Level.SEVERE, "Не удалось загрузить config.yml", e)
        }
    }

    fun reloadConfig() {
        loadConfig()
    }

    fun getInt(path: String, def: Int): Int {
        val config = configuration ?: return def
        return config.getInt(path, def)
    }

    fun getRawMessage(path: String, def: String): String {
        val config = configuration ?: return def
        return config.getString("messages.$path", def)
    }

    fun getMessage(path: String, def: String): Array<BaseComponent> {
        return TextUtils.format(getRawMessage(path, def))
    }

    fun sendMessage(
        sender: CommandSender,
        path: String,
        placeholders: Map<String, String> = emptyMap(),
        defaultLines: List<String> = emptyList()
    ) {
        val config = configuration
        if (config == null) {
            for (line in defaultLines) {
                sender.sendMessage(*TextUtils.format(replacePlaceholders(line, placeholders)))
            }
            return
        }

        when (val obj = config.get("messages.$path")) {
            is List<*> -> {
                for (item in obj) {
                    if (item != null) {
                        sender.sendMessage(*TextUtils.format(replacePlaceholders(item.toString(), placeholders)))
                    }
                }
            }
            is String -> {
                sender.sendMessage(*TextUtils.format(replacePlaceholders(obj, placeholders)))
            }
            else -> {
                for (line in defaultLines) {
                    sender.sendMessage(*TextUtils.format(replacePlaceholders(line, placeholders)))
                }
            }
        }
    }

    private fun replacePlaceholders(text: String, placeholders: Map<String, String>): String {
        if (placeholders.isEmpty()) {
            return text
        }
        var result = text
        for ((key, value) in placeholders) {
            result = result.replace(key, value)
        }
        return result
    }
}
