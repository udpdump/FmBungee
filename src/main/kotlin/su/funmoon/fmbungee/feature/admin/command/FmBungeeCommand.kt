package su.funmoon.fmbungee.feature.admin.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import su.funmoon.fmbungee.FmBungee
import su.funmoon.fmbungee.utils.TextUtils
import kotlin.math.roundToInt

class FmBungeeCommand(private val plugin: FmBungee) : Command("fmbungee", "fmbungee.admin"), TabExecutor {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("fmbungee.admin")) {
            sender.sendMessage(*plugin.configManager.getMessage("no-permission", "&c> &fНедостаточно прав"))
            return
        }

        if (args.isNotEmpty()) {
            if (args[0].equals("reload", ignoreCase = true)) {
                plugin.configManager.reloadConfig()
                sender.sendMessage(*plugin.configManager.getMessage("reload-success", "&a> &fКонфигурация перезагружена"))
                return
            }

            if (args[0].equals("avgping", ignoreCase = true)) {
                handleAvgPing(sender, args)
                return
            }
        }

        plugin.configManager.sendMessage(
            sender,
            "usage-fmbungee",
            defaultLines = listOf(
                "&6> &fКоманды FmBungee:",
                "&7/fmbungee reload &8— &fперезагрузка конфигурации",
                "&7/fmbungee avgping [сервер] &8— &fсредний пинг"
            )
        )
    }

    private fun handleAvgPing(sender: CommandSender, args: Array<out String>) {
        val players: Collection<ProxiedPlayer>
        val targetName: String

        if (args.size >= 2) {
            val serverName = args[1]
            val serverInfo = ProxyServer.getInstance().getServerInfo(serverName)

            if (serverInfo == null) {
                val template = plugin.configManager.getRawMessage("server-not-found", "&c> &fСервер {server} не найден")
                sender.sendMessage(*TextUtils.format(template.replace("{server}", serverName)))
                return
            }

            players = serverInfo.players
            targetName = serverInfo.name
        } else {
            players = ProxyServer.getInstance().players
            targetName = "Вся сеть"
        }

        if (players.isEmpty()) {
            sender.sendMessage(*plugin.configManager.getMessage("avgping-no-players", "&c> &fНет игроков онлайн"))
            return
        }

        val threshold = plugin.configManager.getInt("settings.high-ping-threshold", 150)

        val avgPing = players.map { it.ping }.average().roundToInt()
        val highPingCount = players.count { it.ping >= threshold }

        val placeholders = mapOf(
            "{target}" to targetName,
            "{players}" to players.size.toString(),
            "{avg_ping}" to avgPing.toString(),
            "{high_ping_count}" to highPingCount.toString(),
            "{threshold}" to threshold.toString()
        )

        plugin.configManager.sendMessage(
            sender,
            "avgping",
            placeholders,
            listOf(
                "&6> &fСтатистика &7({target})&f:",
                "&7Онлайн: &f{players}",
                "&7Средний пинг: &a{avg_ping} &7мс",
                "&7Высокий пинг &8(>={threshold}мс)&7: &c{high_ping_count}"
            )
        )
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): Iterable<String> {
        if (!sender.hasPermission("fmbungee.admin")) {
            return emptyList()
        }

        if (args.size == 1) {
            val prefix = args[0].lowercase()
            return listOf("reload", "avgping").filter { it.startsWith(prefix) }
        }

        if (args.size == 2 && args[0].equals("avgping", ignoreCase = true)) {
            val prefix = args[1].lowercase()
            return ProxyServer.getInstance().servers.keys.filter { it.lowercase().startsWith(prefix) }
        }

        return emptyList()
    }
}
