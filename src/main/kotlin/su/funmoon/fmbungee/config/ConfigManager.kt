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

    lateinit var configuration: Configuration
        private set

    private val provider: ConfigurationProvider = ConfigurationProvider.getProvider(YamlConfiguration::class.java)
    private val configFile: File = File(plugin.dataFolder, "config.yml")

    fun loadConfig(): Boolean {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        val defaultConfig = runCatching {
            plugin.getResourceAsStream("config.yml")?.use { input ->
                provider.load(input)
            }
        }.onFailure { e ->
            plugin.logger.log(Level.SEVERE, "Не удалось загрузить встроенный config.yml из ресурсов", e)
        }.getOrNull() ?: Configuration()

        if (!configFile.exists()) {
            try {
                plugin.getResourceAsStream("config.yml")?.use { input ->
                    Files.copy(input, configFile.toPath())
                } ?: configFile.createNewFile()
            } catch (e: IOException) {
                plugin.logger.log(Level.SEVERE, "Не удалось создать config.yml", e)
            }
        }

        return try {
            configuration = provider.load(configFile, defaultConfig)
            true
        } catch (e: IOException) {
            plugin.logger.log(Level.SEVERE, "Не удалось загрузить config.yml с диска", e)
            configuration = defaultConfig
            false
        }
    }

    fun reloadConfig(): Boolean = loadConfig()

    fun getInt(path: String, def: Int? = null): Int {
        return if (def != null) {
            configuration.getInt(path, def)
        } else {
            configuration.getInt(path)
        }
    }

    fun getString(path: String, def: String = ""): String {
        return configuration.getString(path, def)
    }

    fun getRawMessage(path: String, def: String = ""): String {
        return configuration.getString("messages.$path", def)
    }

    fun getMessage(path: String, def: String = ""): Array<BaseComponent> {
        return TextUtils.format(getRawMessage(path, def))
    }

    fun sendMessage(sender: CommandSender, path: String) {
        sendMessage(sender, path, emptyMap(), emptyList())
    }

    fun sendMessage(
        sender: CommandSender,
        path: String,
        placeholder: Pair<String, Any>,
        vararg placeholders: Pair<String, Any>
    ) {
        val map = buildMap {
            put(placeholder.first, placeholder.second)
            for ((k, v) in placeholders) {
                put(k, v)
            }
        }
        sendMessage(sender, path, map, emptyList())
    }

    fun sendMessage(
        sender: CommandSender,
        path: String,
        placeholders: Map<String, Any> = emptyMap(),
        defaultLines: List<String> = emptyList()
    ) {
        val obj = configuration.get("messages.$path")
        if (obj == null) {
            if (defaultLines.isNotEmpty()) {
                for (line in defaultLines) {
                    sender.sendMessage(TextUtils.toComponent(replacePlaceholders(line, placeholders)))
                }
            } else {
                plugin.logger.warning("Сообщение 'messages.$path' не найдено в конфигурации")
            }
            return
        }

        when (obj) {
            is List<*> -> {
                for (item in obj) {
                    if (item != null) {
                        val formatted = replacePlaceholders(item.toString(), placeholders)
                        sender.sendMessage(TextUtils.toComponent(formatted))
                    }
                }
            }
            else -> {
                val formatted = replacePlaceholders(obj.toString(), placeholders)
                sender.sendMessage(TextUtils.toComponent(formatted))
            }
        }
    }

    fun replacePlaceholders(text: String, placeholders: Map<String, Any>): String {
        if (placeholders.isEmpty()) {
            return text
        }
        var result = text
        for ((key, value) in placeholders) {
            result = result.replace(key, value.toString())
        }
        return result
    }
}

fun CommandSender.sendConfigMessage(
    configManager: ConfigManager,
    path: String,
    vararg placeholders: Pair<String, Any>
) {
    if (placeholders.isEmpty()) {
        configManager.sendMessage(this, path)
    } else {
        configManager.sendMessage(this, path, placeholders.toMap())
    }
}

