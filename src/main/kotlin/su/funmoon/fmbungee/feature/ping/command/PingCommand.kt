package su.funmoon.fmbungee.feature.ping.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import su.funmoon.fmbungee.FmBungee
import su.funmoon.fmbungee.utils.sendConfigMessage

class PingCommand(private val plugin: FmBungee) : Command("ping", null, "p"), TabExecutor {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.isEmpty()) {
            if (sender !is ProxiedPlayer) {
                sender.sendConfigMessage(plugin, "only-players")
                return
            }

            sender.sendConfigMessage(plugin, "ping", "{ping}" to sender.ping)
            return
        }

        if (!sender.hasPermission("fmbungee.ping.other")) {
            sender.sendConfigMessage(plugin, "no-permission")
            return
        }

        val targetName = args[0]
        val target = plugin.proxy.getPlayer(targetName)

        if (target == null) {
            sender.sendConfigMessage(plugin, "player-not-found", "{player}" to targetName)
            return
        }

        sender.sendConfigMessage(
            plugin,
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

