package su.funmoon.fmbungee.feature.admin.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import su.funmoon.fmbungee.FmBungee
import kotlin.math.roundToInt

class FmBungeeCommand(private val plugin: FmBungee) : Command("fmbungee", null), TabExecutor {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("fmbungee.admin")) {
            plugin.configManager.sendMessage(sender, "no-permission")
            return
        }

        when (args.firstOrNull()?.lowercase()) {
            "reload" -> handleReload(sender)
            "avgping" -> handleAvgPing(sender, args)
            else -> plugin.configManager.sendMessage(sender, "usage-fmbungee")
        }
    }

    private fun handleReload(sender: CommandSender) {
        if (plugin.configManager.reloadConfig()) {
            plugin.configManager.sendMessage(sender, "reload-success")
        } else {
            plugin.configManager.sendMessage(sender, "reload-fail")
        }
    }

    private fun handleAvgPing(sender: CommandSender, args: Array<out String>) {
        val players: Collection<ProxiedPlayer>
        val targetName: String

        if (args.size >= 2) {
            val serverName = args[1]
            val serverInfo = plugin.proxy.getServerInfo(serverName)

            if (serverInfo == null) {
                plugin.configManager.sendMessage(sender, "server-not-found", "{server}" to serverName)
                return
            }

            players = serverInfo.players
            targetName = serverInfo.name
        } else {
            players = plugin.proxy.players
            targetName = plugin.configManager.getString("messages.target-all-network", "Вся сеть")
        }

        if (players.isEmpty()) {
            plugin.configManager.sendMessage(sender, "avgping-no-players")
            return
        }

        val threshold = plugin.configManager.getInt("settings.high-ping-threshold", 150)
        val avgPing = (players.sumOf { it.ping.toLong() }.toDouble() / players.size).roundToInt()
        val highPingCount = players.count { it.ping >= threshold }

        plugin.configManager.sendMessage(
            sender,
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

