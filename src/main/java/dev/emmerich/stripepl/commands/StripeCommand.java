package dev.emmerich.stripepl.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.emmerich.stripepl.Main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StripeCommand extends Command {

    private final Main plugin;

    public StripeCommand(Main plugin) {
        super("stripepl");
        this.plugin = plugin;
        this.setDescription("Main command for StripePL plugin.");
        this.setUsage("/<command>");
        this.setPermission("stripepl.use");
        this.setPermissionMessage("You don't have permission to use this command.");
        this.setAliases(Arrays.asList("spl", "stripe")); // Example aliases
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage("You used the StripePL command!");
        } else {
            sender.sendMessage("StripePL command can only be used by players for now.");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        // Basic tab completion example
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            return completions;
        }
        return super.tabComplete(sender, alias, args);
    }
}
