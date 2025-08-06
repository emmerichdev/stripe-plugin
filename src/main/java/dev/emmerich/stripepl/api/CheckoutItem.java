package dev.emmerich.stripepl.api;

public class CheckoutItem {

    private final String priceId;
    private final long quantity;

    /**
     * Represents an item to be included in a Stripe Checkout Session.
     *
     * @param priceId  The ID of the Stripe Price for this item.
     * @param quantity The quantity of the item to purchase.
     */
    public CheckoutItem(String priceId, long quantity) {
        this.priceId = priceId;
        this.quantity = quantity;
    }

    public String getPriceId() {
        return priceId;
    }

    public long getQuantity() {
        return quantity;
    }
}