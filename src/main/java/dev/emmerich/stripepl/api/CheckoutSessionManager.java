package dev.emmerich.stripepl.api;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class CheckoutSessionManager {

    private CheckoutSessionManager() {}

    /**
     * Creates a new Stripe Checkout Session for the given player and items.
     *
     * @param player      The player to create the checkout session for.
     * @param items       The list of items to include in the checkout.
     * @param successUrl  The URL to redirect the player to after a successful checkout.
     * @param cancelUrl   The URL to redirect the player to after a canceled checkout.
     * @return The created Stripe Checkout Session.
     * @throws StripeException If there is an error creating the checkout session.
     */
    public static Session createCheckoutSession(Player player, List<CheckoutItem> items, String successUrl, String cancelUrl) throws StripeException {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be null or empty.");
        }
        if (successUrl == null || successUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Success URL cannot be null or empty.");
        }
        if (cancelUrl == null || cancelUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancel URL cannot be null or empty.");
        }

        List<SessionCreateParams.LineItem> lineItems = items.stream()
                .map(item -> SessionCreateParams.LineItem.builder()
                        .setPrice(item.getPriceId())
                        .setQuantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addAllLineItem(lineItems)
                .putMetadata("minecraft_username", player.getName())
                .build();

        return Session.create(params);
    }
}