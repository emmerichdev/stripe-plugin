# StripePL

## Overview

StripePL is a plugin that allows developers to integrate Stripe for in-game purchases. Players can purchase items using real money through Stripe's checkout process, and the plugin will automatically run commands upon successful payment.

## Features

*   **Stripe Integration:** Connects to your Stripe account to process payments.
*   **Web-Based Checkout:** Creates Stripe Checkout sessions for a seamless and secure payment experience.
*   **Webhook Listener:** Listens for Stripe webhooks to confirm payments and grant purchases.
*   **Customizable Commands:** Configure commands to be executed when a player purchases a product.
*   **MongoDB Integration:** Stores processed event data in MongoDB.

## Configuration

```yaml
stripe-api-key: "api-key" # get it from your stripe store (testing or production)
stripe-webhook-secret: "stripe-webhook-secret" # get it from stripe or stripe cli (for testing)

# embedded webhook listener
webhook:
  port: 8000
  path: "/stripe/webhook"

# storage settings (MongoDB required)
storage:
  mongo:
    uri: "mongodb://localhost:27017"
    database: "stripepl"
    collection: "processed_events"
    ttlDays: 30

# these are the commands the server will run when your products are purchased
product-commands:
  prod_SnshrV0CJQ5VLL: # replace with a real product id from the products page
    - "give {player} minecraft:diamond 1"

# this is just meant to be an example command, please use the api to implement checking out properly
checkout-command-item:
  price-id: "price_1RsGwGDQBE4OBO7HzBLUmDa9" # replace with price from the products page under pricing where the 3 dots are
  success-url: "https://example.com/success"
  cancel-url: "https://example.com/cancel"
```

## Installation

1.  Download the latest release of the plugin.
2.  Place the `.jar` file in your server's `plugins` directory.
3.  Restart your server.
4.  Configure the `config.yml` file with your Stripe API key, webhook secret, and other settings.
5.  Restart the server again or use `/stripe reload` to apply the changes.