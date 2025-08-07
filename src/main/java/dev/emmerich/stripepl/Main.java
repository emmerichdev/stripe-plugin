package dev.emmerich.stripepl;

import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;
import com.stripe.Stripe;
import dev.emmerich.stripepl.commands.StripeCommand;
import dev.emmerich.stripepl.webhook.StripeWebhookHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import co.aikar.commands.BukkitCommandManager;

public class Main extends JavaPlugin {

    private static Main instance;
    private HttpServer server;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("StripePL has been enabled!");

        saveDefaultConfig(); // Create config.yml if it doesn't exist
        String stripeWebhookSecret = getConfig().getString("stripe-webhook-secret");
        String stripeApiKey = getConfig().getString("stripe-api-key");

        if (stripeApiKey == null || stripeApiKey.isEmpty() || stripeApiKey.equals("api-key")) {
            getLogger().severe("Stripe API key is not configured properly. Please update config.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (stripeWebhookSecret == null || stripeWebhookSecret.isEmpty() || stripeWebhookSecret.equals("secret")) {
            getLogger().severe("Stripe webhook secret is not configured properly. Please update config.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Stripe.apiKey = stripeApiKey;

        // Load product commands
        Map<String, List<String>> productCommands = new HashMap<>();
        ConfigurationSection productCommandsSection = getConfig().getConfigurationSection("product-commands");
        if (productCommandsSection != null) {
            for (String productId : productCommandsSection.getKeys(false)) {
                productCommands.put(productId, productCommandsSection.getStringList(productId));
            }
        }

        BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.registerCommand(new StripeCommand(this));


        try {
            int WEBHOOK_PORT = 8000;
            server = HttpServer.create(new InetSocketAddress(WEBHOOK_PORT), 0);
            server.createContext("/stripe/webhook", new StripeWebhookHandler(this, stripeWebhookSecret, productCommands));
            server.setExecutor(Executors.newFixedThreadPool(5)); // Create a pool of 5 threads
            server.start();
            getLogger().info("Stripe Webhook Listener started on port " + WEBHOOK_PORT);
        } catch (IOException e) {
            getLogger().severe("Failed to start Stripe Webhook Listener: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.stop(0);
            getLogger().info("Stripe Webhook Listener stopped.");
        }
        getLogger().info("StripePL has been disabled!");
    }

    public static Main getInstance() {
        return instance;
    }
}
