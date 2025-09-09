package com.radovan.play.brokers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.play.exceptions.InvalidCartException;
import com.radovan.play.utils.NatsUtils;
import io.nats.client.Connection;
import io.nats.client.Message;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.StreamSupport;

@Singleton
public class OrderNatsSender {

    private static final int REQUEST_TIMEOUT_SECONDS = 5;

    private ObjectMapper objectMapper;
    private NatsUtils natsUtils;

    @Inject
    private void initialize(ObjectMapper objectMapper, NatsUtils natsUtils) {
        this.objectMapper = objectMapper;
        this.natsUtils = natsUtils;
    }

    public JsonNode retrieveProductFromBroker(int productId, String jwtToken) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("token", jwtToken);
        payload.put("productId", productId);

        String response = sendRequest("product.get." + productId, payload.toString());
        JsonNode json;
        try {
            json = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }

        if (json.has("status") && json.get("status").asInt() == 422) {
            String msg = json.has("message") ? json.get("message").asText() : "Product not found";
            throw new RuntimeException(msg);
        }

        return json;
    }

    public JsonNode retrieveCurrentCustomer(String jwtToken) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("token", jwtToken);

        String response = sendRequest("customer.getCurrent", payload.toString());
        JsonNode json;
        try {
            json = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }

        if (json.has("status") && json.get("status").asInt() == 500) {
            String msg = json.has("error") ? json.get("error").asText() : "Failed to retrieve current customer";
            throw new RuntimeException(msg);
        }

        return json;
    }

    public JsonNode validateCart(int cartId, String jwtToken) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("token", jwtToken);
        payload.put("cartId", cartId);

        String response = sendRequest("cart.validate." + cartId, payload.toString());
        JsonNode json;
        try {
            json = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }

        int status = Optional.ofNullable(json.get("status")).map(JsonNode::asInt).orElse(200);
        if (status == 406) {
            String msg = Optional.ofNullable(json.get("message")).map(JsonNode::asText).orElse("Cart is invalid");
            throw new InvalidCartException(msg);
        } else if (status == 500) {
            String msg = Optional.ofNullable(json.get("message")).map(JsonNode::asText).orElse("Server error during cart validation");
            throw new RuntimeException(msg);
        }

        return json;
    }

    public JsonNode retrieveAddress(int addressId, String jwtToken) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("token", jwtToken);
        payload.put("addressId", addressId);

        String response = sendRequest("address.getAddress." + addressId, payload.toString());
        JsonNode json;
        try {
            json = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }

        if (json.has("status") && json.get("status").asInt() == 500) {
            String msg = json.has("error") ? json.get("error").asText() : "Failed to retrieve address";
            throw new RuntimeException(msg);
        }

        return json;
    }

    public JsonNode updateShippingAddress(JsonNode address, int addressId, String jwtToken) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.set("address", address);
        payload.put("Authorization", jwtToken);

        String subject = "address.update." + addressId;
        String response;
        try {
            response = sendRequest(subject, objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        JsonNode json;
        try {
            json = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }

        int status = Optional.ofNullable(json.get("status")).map(JsonNode::asInt).orElse(200);
        if (status == 500) {
            String msg = Optional.ofNullable(json.get("message")).map(JsonNode::asText).orElse("Address update failed");
            throw new RuntimeException(msg);
        }

        return json;
    }

    public JsonNode[] retrieveCartItems(int cartId, String jwtToken) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("token", jwtToken);
        payload.put("cartId", cartId);

        String response = sendRequest("cart.getItems." + cartId, payload.toString());
        JsonNode json;
        try {
            json = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }

        if (json.has("status") && json.get("status").asInt() == 500) {
            String msg = json.has("error") ? json.get("error").asText() : "Failed to retrieve cart items";
            throw new RuntimeException(msg);
        }

        JsonNode itemsNode;
        if (json.isArray()) {
            itemsNode = json;
        } else if (json.has("items") && json.get("items").isArray()) {
            itemsNode = json.get("items");
        } else {
            throw new RuntimeException("Expected array of cart items, but got: " + json.getNodeType());
        }

        return StreamSupport.stream(itemsNode.spliterator(), false)
                .toArray(JsonNode[]::new);
    }

    public JsonNode updateProductViaBroker(ObjectNode productNode, int productId, String jwtToken) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.set("product", productNode);
        payload.put("Authorization", jwtToken);

        String subject = "product.update." + productId;
        String response;
        try {
            response = sendRequest(subject, objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        JsonNode json;
        try {
            json = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }

        if (json.has("status") && json.get("status").asInt() == 500) {
            String msg = json.has("message") ? json.get("message").asText() : "Product update failed";
            throw new RuntimeException(msg);
        }

        return json;
    }

    public JsonNode removeAllByCartId(int cartId, String jwtToken) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("Authorization", jwtToken);
        payload.put("cartId", cartId);

        String response = sendRequest("cart.removeAllByCartId." + cartId, payload.toString());
        JsonNode json;
        try {
            json = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }

        if (json.has("status") && json.get("status").asInt() == 500) {
            String msg = json.has("message") ? json.get("message").asText() : "Failed to remove items from cart";
            throw new RuntimeException(msg);
        }

        return json;
    }

    public JsonNode refreshCartState(int cartId, String jwtToken) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("Authorization", jwtToken);
        payload.put("cartId", cartId);

        String response = sendRequest("cart.refreshState." + cartId, payload.toString());
        JsonNode json;
        try {
            json = objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }

        if (json.has("status") && json.get("status").asInt() == 500) {
            String msg = json.has("message") ? json.get("message").asText() : "Failed to refresh cart state";
            throw new RuntimeException(msg);
        }

        return json;
    }

    private String sendRequest(String subject, String payload) {
        Connection connection = natsUtils.getConnection();
        if (connection == null) {
            throw new RuntimeException("NATS connection is not initialized");
        }

        try {
            CompletableFuture<Message> future = connection.request(
                    subject,
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            Message msg = future.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return new String(msg.getData(), StandardCharsets.UTF_8);
        } catch (TimeoutException e) {
            throw new RuntimeException("NATS request timeout for subject: " + subject, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("NATS request interrupted for subject: " + subject, e);
        } catch (Exception e) {
            throw new RuntimeException("NATS request failed for subject: " + subject, e);
        }
    }
}