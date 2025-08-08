package dev.emmerich.stripepl.service;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GrantDispatcher {

    private GrantDispatcher() {}

    public static void dispatchForPurchase(
            JavaPlugin plugin,
            Map<String, List<String>> productCommands,
            String playerName,
            String productId,
            long quantity,
            String eventId
    ) {
        List<String> commandsToRun = new ArrayList<>();
        if (productId != null && productCommands != null) {
            commandsToRun.addAll(productCommands.getOrDefault(productId, Collections.emptyList()));
        }

        if (commandsToRun.isEmpty()) {
            plugin.getLogger().warning("No commands configured for product: productId=" + productId);
            return;
        }

        for (String cmd : commandsToRun) {
            String finalCommand = cmd
                    .replace("{player}", playerName != null ? playerName : "")
                    .replace("{product_id}", productId != null ? productId : "")
                    .replace("{qty}", String.valueOf(quantity))
                    .replace("{event_id}", eventId != null ? eventId : "");
            plugin.getLogger().info("Dispatching command: " + finalCommand);
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(), finalCommand));
        }
    }
}


