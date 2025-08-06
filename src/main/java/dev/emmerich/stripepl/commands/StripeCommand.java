package dev.emmerich.stripepl.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.stripe.exception.StripeException;
import dev.emmerich.stripepl.api.CheckoutItem;
import dev.emmerich.stripepl.Main;
import dev.emmerich.stripepl.api.CheckoutSessionManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

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

    @Subcommand("checkout")
    @Description("Creates a Stripe checkout session for the player.")
    @CommandPermission("stripepl.checkout")
    public void onCheckout(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String priceId = plugin.getConfig().getString("checkout-command-item.price-id");
            String successUrl = plugin.getConfig().getString("checkout-command-item.success-url");
            String cancelUrl = plugin.getConfig().getString("checkout-command-item.cancel-url");

            if (priceId == null || priceId.isEmpty() || priceId.equals("price_1Pb2L4RxH3d5a2zmsu33mIEg")) {
                player.sendMessage("The checkout item is not configured correctly. Please contact an administrator.");
                return;
            }

            if (successUrl == null || successUrl.isEmpty() || cancelUrl == null || cancelUrl.isEmpty()) {
                player.sendMessage("The success and cancel URLs are not configured correctly. Please contact an administrator.");
                return;
            }

            CheckoutItem item = new CheckoutItem(priceId, 1);
            try {
                com.stripe.model.checkout.Session session = CheckoutSessionManager.createCheckoutSession(player, Collections.singletonList(item), successUrl, cancelUrl);
                player.sendMessage("Click here to checkout: " + session.getUrl());
            } catch (StripeException e) {
                player.sendMessage("Error creating checkout session: " + e.getMessage());
            }
        } else {
            sender.sendMessage("This command can only be used by players.");
        }
    }
}
