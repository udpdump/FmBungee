package su.funmoon.fmbungee.feature.ping.command

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import su.funmoon.fmbungee.FmBungee
import su.funmoon.fmbungee.utils.TextUtils

class PingCommand(private val plugin: FmBungee) : Command("ping", null, "p"), TabExecutor {

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.isEmpty()) {
            if (sender !is ProxiedPlayer) {
                sender.sendMessage(*plugin.configManager.getMessage("only-players", "&c> &fИспользуйте: /ping <игрок>"))
                return
            }

            val ping = sender.ping
            val messageTemplate = plugin.configManager.getRawMessage("ping", "&7Пинг: &a{ping} &7мс")
            val formatted = messageTemplate.replace("{ping}", ping.toString())
            sender.sendMessage(*TextUtils.format(formatted))
            return
        }

        if (sender is ProxiedPlayer && !sender.hasPermission("fmbungee.ping.other")) {
            sender.sendMessage(*plugin.configManager.getMessage("no-permission", "&c> &fНедостаточно прав"))
            return
        }

        val targetName = args[0]
        val target = ProxyServer.getInstance().getPlayer(targetName)

        if (target == null) {
            val messageTemplate = plugin.configManager.getRawMessage("player-not-found", "&c> &fИгрок {player} не найден")
            val formatted = messageTemplate.replace("{player}", targetName)
            sender.sendMessage(*TextUtils.format(formatted))
            return
        }

        val messageTemplate = plugin.configManager.getRawMessage("ping-other", "&7Пинг &f{player}&7: &a{ping} &7мс")
        val formatted = messageTemplate
            .replace("{player}", target.name)
            .replace("{ping}", target.ping.toString())
        sender.sendMessage(*TextUtils.format(formatted))
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): Iterable<String> {
        if (args.size == 1) {
            val prefix = args[0].lowercase()
            return ProxyServer.getInstance().players
                .map { it.name }
                .filter { it.lowercase().startsWith(prefix) }
        }
        return emptyList()
    }
}
