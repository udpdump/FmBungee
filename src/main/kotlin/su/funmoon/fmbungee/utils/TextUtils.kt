package su.funmoon.fmbungee.utils

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import java.util.regex.Pattern

object TextUtils {

    private val HEX_PATTERN = Pattern.compile("&?#([A-Fa-f0-9]{6})")

    @JvmStatic
    fun colorize(text: String?): String {
        if (text == null) return ""
        val matcher = HEX_PATTERN.matcher(text)
        val buffer = StringBuilder(text.length + 32)

        while (matcher.find()) {
            val group = matcher.group(1)
            matcher.appendReplacement(
                buffer,
                "§x§${group[0]}§${group[1]}§${group[2]}§${group[3]}§${group[4]}§${group[5]}"
            )
        }
        matcher.appendTail(buffer)
        return ChatColor.translateAlternateColorCodes('&', buffer.toString())
    }

    @JvmStatic
    @Suppress("DEPRECATION")
    fun format(text: String?): Array<BaseComponent> {
        return TextComponent.fromLegacyText(colorize(text))
    }

    @JvmStatic
    fun toComponent(text: String?): BaseComponent {
        return TextComponent.fromLegacy(colorize(text))
    }
}

fun String.colorize(): String = TextUtils.colorize(this)

fun String.toComponent(): BaseComponent = TextUtils.toComponent(this)

fun String.toComponents(): Array<BaseComponent> = TextUtils.format(this)

fun CommandSender.sendColoredMessage(text: String) {
    sendMessage(TextUtils.toComponent(text))
}

