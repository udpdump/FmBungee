package su.funmoon.fmbungee.feature.admin.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import su.funmoon.fmbungee.FmBungee
import su.funmoon.fmbungee.utils.sendConfigMessage
import kotlin.math.roundToInt

class FmBungeeCommand(private val plugin: FmBungee) : Command("fmbungee", null), TabExecutor {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("fmbungee.admin")) {
            sender.sendConfigMessage(plugin, "no-permission")
            return
        }

        when (args.firstOrNull()?.lowercase()) {
            "reload" -> handleReload(sender)
            "avgping" -> handleAvgPing(sender, args)
            else -> sender.sendConfigMessage(plugin, "usage-fmbungee")
        }
    }

    private fun handleReload(sender: CommandSender) {
        if (plugin.configManager.reloadConfig()) {
            sender.sendConfigMessage(plugin, "reload-success")
        } else {
            sender.sendConfigMessage(plugin, "reload-fail")
        }
    }

    private fun handleAvgPing(sender: CommandSender, args: Array<out String>) {
        val players: Collection<ProxiedPlayer>
        val targetName: String

        if (args.size >= 2) {
            val serverName = args[1]
            val serverInfo = plugin.proxy.getServerInfo(serverName)

            if (serverInfo == null) {
                sender.sendConfigMessage(plugin, "server-not-found", "{server}" to serverName)
                return
            }

            players = serverInfo.players
            targetName = serverInfo.name
        } else {
            players = plugin.proxy.players
            targetName = plugin.configManager.getString("messages.target-all-network", "Вся сеть")
        }

        if (players.isEmpty()) {
            sender.sendConfigMessage(plugin, "avgping-no-players")
            return
        }

        val threshold = plugin.configManager.getInt("settings.high-ping-threshold", 150)
        val avgPing = (players.sumOf { it.ping.toLong() }.toDouble() / players.size).roundToInt()
        val highPingCount = players.count { it.ping >= threshold }

        sender.sendConfigMessage(
            plugin,
            "avgping",
            "{target}" to targetName,
            "{players}" to players.size,
            "{avg_ping}" to avgPing,
            "{high_ping_count}" to highPingCount,
            "{threshold}" to threshold
        )
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): Iterable<String> {
        if (!sender.hasPermission("fmbungee.admin")) {
            return emptyList()
        }

        if (args.size == 1) {
            val prefix = args[0]
            return listOf("reload", "avgping").filter { it.startsWith(prefix, ignoreCase = true) }
        }

        if (args.size == 2 && args[0].equals("avgping", ignoreCase = true)) {
            val prefix = args[1]
            return plugin.proxy.servers.keys.filter { it.startsWith(prefix, ignoreCase = true) }
        }

        return emptyList()
    }
}

