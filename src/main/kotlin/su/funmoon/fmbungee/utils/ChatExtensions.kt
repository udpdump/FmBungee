package su.funmoon.fmbungee.utils

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.BaseComponent
import su.funmoon.fmbungee.FmBungee
import su.funmoon.fmbungee.config.ConfigManager

fun String.colorize(): String = TextUtils.colorize(this)

fun String.toComponent(): BaseComponent = TextUtils.toComponent(this)

fun String.toComponents(): Array<BaseComponent> = TextUtils.format(this)

fun CommandSender.sendColoredMessage(text: String) {
    sendMessage(TextUtils.toComponent(text))
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

fun CommandSender.sendConfigMessage(
    plugin: FmBungee,
    path: String,
    vararg placeholders: Pair<String, Any>
) {
    sendConfigMessage(plugin.configManager, path, *placeholders)
}
