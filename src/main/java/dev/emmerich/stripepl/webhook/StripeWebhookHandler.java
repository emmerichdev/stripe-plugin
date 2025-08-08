package dev.emmerich.stripepl.webhook;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.emmerich.stripepl.service.GrantDispatcher;
import dev.emmerich.stripepl.storage.ProcessedEventStore;
import org.bukkit.plugin.java.JavaPlugin;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.model.LineItemCollection;
import com.stripe.model.LineItem;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;
import com.stripe.model.StripeObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StripeWebhookHandler implements HttpHandler {
    private final JavaPlugin plugin;
    private final String webhookSecret;
    private final Map<String, List<String>> productCommands;
    private final ProcessedEventStore processedEventStore;

    public StripeWebhookHandler(JavaPlugin plugin,
                                String webhookSecret,
                                Map<String, List<String>> productCommands,
                                ProcessedEventStore processedEventStore) {
        this.plugin = plugin;
        this.webhookSecret = webhookSecret;
        this.productCommands = productCommands;
        this.processedEventStore = processedEventStore;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int statusCode = 200;

        try {
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                    .lines().collect(Collectors.joining("\n"));

            String stripeSignature = exchange.getRequestHeaders().getFirst("Stripe-Signature");

            if (stripeSignature == null) {
                plugin.getLogger().warning("Missing Stripe-Signature header.");
                response = "Missing Stripe-Signature header.";
                statusCode = 400;
            } else {
                Event event = Webhook.constructEvent(requestBody, stripeSignature, webhookSecret);
                String eventId = event.getId();

                if (eventId != null && processedEventStore.hasProcessed(eventId)) {
                    plugin.getLogger().info("Ignoring already processed Stripe Event: " + eventId);
                    response = "Already processed.";
                } else {
                    plugin.getLogger().info("Received Stripe Event: " + event.getType());

                    if ("checkout.session.completed".equals(event.getType())) {
                        Optional<StripeObject> dataObject = event.getDataObjectDeserializer().getObject();
                        if (dataObject.isPresent() && dataObject.get() instanceof Session) {
                            Session session = (Session) dataObject.get();
                            String minecraftUsername = session.getMetadata().get("minecraft_username");

                            if (minecraftUsername != null) {
                                Map<String, Object> listLineItemsParams = new HashMap<>();
                                listLineItemsParams.put("limit", 100);
                                LineItemCollection lineItems = session.listLineItems(listLineItemsParams);

                                for (LineItem lineItem : lineItems.getData()) {
                                    String productId = lineItem.getPrice().getProduct();
                                    long quantity = lineItem.getQuantity() != null ? lineItem.getQuantity() : 1L;

                                    GrantDispatcher.dispatchForPurchase(
                                            plugin,
                                            productCommands,
                                            minecraftUsername,
                                            productId,
                                            quantity,
                                            eventId
                                    );
                                }

                                if (eventId != null) {
                                    processedEventStore.markProcessed(eventId);
                                }
                            } else {
                                plugin.getLogger().warning("Checkout session completed, but 'minecraft_username' metadata is missing.");
                            }
                        } else {
                            plugin.getLogger().warning("Event data object is not a Session or is not present.");
                        }
                    }
                    response = "Webhook processed.";
                }
            }

        } catch (SignatureVerificationException e) {
            plugin.getLogger().warning("Stripe webhook signature verification failed: " + e.getMessage());
            response = "Signature verification failed.";
            statusCode = 400;
        } catch (Exception e) {
            plugin.getLogger().severe("Error processing Stripe webhook: " + e.getMessage());
            response = "Internal server error.";
            statusCode = 500;
        } finally {
            exchange.sendResponseHeaders(statusCode, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}