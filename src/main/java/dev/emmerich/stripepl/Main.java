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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import java.lang.reflect.Field;

public class Main extends JavaPlugin {

    private HttpServer server;
    private final int WEBHOOK_PORT = 8000; // You can change this port
    private String stripeWebhookSecret;
    private String stripeApiKey;
    private Map<String, List<String>> productCommands;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("StripePL has been enabled!");

        // Load configuration
        saveDefaultConfig(); // Creates config.yml if it doesn't exist
        stripeWebhookSecret = getConfig().getString("stripe-webhook-secret");
        stripeApiKey = getConfig().getString("stripe-api-key");

        if (stripeWebhookSecret == null || stripeWebhookSecret.equals("whsec_YOUR_WEBHOOK_SECRET_HERE")) {
            getLogger().warning("Stripe webhook secret not configured! Please update config.yml");
        }

        if (stripeApiKey != null) {
            Stripe.apiKey = stripeApiKey;
        }

        // Load product commands
        productCommands = new HashMap<>();
        ConfigurationSection productCommandsSection = getConfig().getConfigurationSection("product-commands");
        if (productCommandsSection != null) {
            for (String productId : productCommandsSection.getKeys(false)) {
                productCommands.put(productId, productCommandsSection.getStringList(productId));
            }
        }

        // Register command programmatically
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            StripeCommand stripeCommand = new StripeCommand(this);
            commandMap.register(this.getName(), stripeCommand);
            getLogger().info("Command /stripepl registered programmatically.");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLogger().severe("Failed to register command programmatically: " + e.getMessage());
        }

        try {
            server = HttpServer.create(new InetSocketAddress(WEBHOOK_PORT), 0);
            server.createContext("/stripe/webhook", new StripeWebhookHandler(this, stripeWebhookSecret, productCommands));
            server.setExecutor(Executors.newFixedThreadPool(5)); // Creates a thread pool with 5 threads
            server.start();
            getLogger().info("Stripe Webhook Listener started on port " + WEBHOOK_PORT);
        } catch (IOException e) {
            getLogger().severe("Failed to start Stripe Webhook Listener: " + e.getMessage());
            // Optionally disable the plugin if the server can't start
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (server != null) {
            server.stop(0); // Stop the server immediately
            getLogger().info("Stripe Webhook Listener stopped.");
        }
        getLogger().info("StripePL has been disabled!");
    }
}
