package dev.emmerich.stripepl.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.emmerich.stripepl.Main;

@CommandAlias("stripepl|spl|stripe")
public class StripeCommand extends BaseCommand {

    private final Main plugin;

    public StripeCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Default
    @Description("Main command for StripePL plugin.")
    @CommandPermission("stripepl.use")
    public void onDefault(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage("You used the StripePL command!");
        } else {
            sender.sendMessage("StripePL command can only be used by players for now.");
        }
    }

    @Subcommand("reload")
    @Description("Reloads the StripePL configuration.")
    @CommandPermission("stripepl.reload")
    public void onReload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage("StripePL configuration reloaded.");
    }
}
