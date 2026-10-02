package su.funmoon.fmbungee.feature.ping.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import su.funmoon.fmbungee.FmBungee

class PingCommand(private val plugin: FmBungee) : Command("ping", null, "p"), TabExecutor {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.isEmpty()) {
            if (sender !is ProxiedPlayer) {
                plugin.configManager.sendMessage(sender, "only-players")
                return
            }

            plugin.configManager.sendMessage(sender, "ping", "{ping}" to sender.ping)
            return
        }

        if (!sender.hasPermission("fmbungee.ping.other")) {
            plugin.configManager.sendMessage(sender, "no-permission")
            return
        }

        val targetName = args[0]
        val target = plugin.proxy.getPlayer(targetName)

        if (target == null) {
            plugin.configManager.sendMessage(sender, "player-not-found", "{player}" to targetName)
            return
        }

        plugin.configManager.sendMessage(
            sender,
            "ping-other",
            "{player}" to target.name,
            "{ping}" to target.ping
        )
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): Iterable<String> {
        if (args.size == 1 && sender.hasPermission("fmbungee.ping.other")) {
            val prefix = args[0]
            return plugin.proxy.players
                .map { it.name }
                .filter { it.startsWith(prefix, ignoreCase = true) }
        }
        return emptyList()
    }
}

