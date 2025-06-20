package com.radovan.play.brokers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.play.exceptions.InvalidCartException;
import com.radovan.play.utils.NatsUtils;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import io.nats.client.Connection;


import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Singleton
public class OrderNatsSender {

    private static final int REQUEST_TIMEOUT_MS = 5000;
    private static final String CONTENT_TYPE = "application/json";
    private static final String SHIPPING_ADDRESS_PREFIX = "shippingAddress.get.";
    private static final String SHIPPING_ADDRESS_UPDATE_PREFIX = "shippingAddress.update.";
    private static final String CART_ITEMS_PREFIX = "cart.items.list.";
    private static final String PRODUCT_GET_PREFIX = "product.get.";
    private static final String PRODUCT_UPDATE_PREFIX = "product.update.";
    private static final String CART_REFRESH_PREFIX = "cart.refresh.";
    private static final String CART_REMOVE_ITEMS_PREFIX = "cart.items.removeAll.";

    private final NatsUtils natsUtils;
    private final ObjectMapper objectMapper;

    @Inject
    public OrderNatsSender(NatsUtils natsUtils, ObjectMapper objectMapper) {
        this.natsUtils = natsUtils;
        this.objectMapper = objectMapper;
    }

    public JsonNode retrieveCustomer(String jwtToken) {
        try {
            Headers headers = createAuthHeaders(jwtToken);
            Message response = sendRequest("customer.get", headers);
            return parseResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch customer data", e);
        }
    }


    public JsonNode validateCart(Integer cartId, String jwtToken) {
        try {
            Headers headers = createAuthHeaders(jwtToken);
            String subject = "cart.validate." + cartId;
            Message response = sendRequest(subject, headers);
            JsonNode responseNode = parseResponse(response);

            if (responseNode.has("error")) {
                // Proveravamo da li je 406 greška
                if (responseNode.has("status") &&
                        responseNode.get("status").asInt() == 406) {
                    throw new InvalidCartException(responseNode.get("message").asText());
                }
                throw new RuntimeException(responseNode.get("error").asText());
            }

            return responseNode.get("cart");
        } catch (InvalidCartException e) {
            // Prosleđujemo InvalidCartException direktno
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate cart: " + e.getMessage(), e);
        }
    }


    public JsonNode retrieveShippingAddress(Integer addressId, String jwtToken) {
        try {
            Headers headers = createAuthHeaders(jwtToken);
            Message response = sendRequest(SHIPPING_ADDRESS_PREFIX + addressId, headers);
            JsonNode responseNode = parseResponse(response);

            if (responseNode.has("error")) {
                throw new RuntimeException(responseNode.get("error").asText());
            }
            return responseNode.get("shippingAddress");
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve shipping address: " + e.getMessage(), e);
        }
    }

    public JsonNode updateShippingAddress(Integer addressId, JsonNode addressData, String jwtToken) {
        try {
            Headers headers = createAuthHeaders(jwtToken);
            byte[] payload = objectMapper.writeValueAsBytes(addressData);
            Message response = natsUtils.getConnection()
                    .request(SHIPPING_ADDRESS_UPDATE_PREFIX + addressId, headers, payload)
                    .get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            JsonNode responseNode = parseResponse(response);
            if (responseNode.has("error")) {
                throw new RuntimeException(responseNode.get("error").asText());
            }
            return responseNode.get("shippingAddress");
        } catch (Exception e) {
            throw new RuntimeException("Failed to update shipping address: " + e.getMessage(), e);
        }
    }

    public List<JsonNode> retrieveCartItems(Integer cartId, String jwtToken) {
        try {
            Headers headers = createAuthHeaders(jwtToken);
            Message response = sendRequest(CART_ITEMS_PREFIX + cartId, headers);
            JsonNode responseNode = parseResponse(response);

            if (responseNode.has("error")) {
                throw new RuntimeException(responseNode.get("error").asText());
            }

            return objectMapper.convertValue(
                    responseNode.get("cartItems"),
                    new TypeReference<List<JsonNode>>() {
                    }
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve cart items: " + e.getMessage(), e);
        }
    }

    public JsonNode retrieveProduct(Integer productId) {
        try {
            Headers headers = createBasicHeaders();
            Message response = natsUtils.getConnection()
                    .request("product.get." + productId, headers, new byte[0])
                    .get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            JsonNode product = parseResponse(response);
            if (product == null || product.has("error")) {
                throw new RuntimeException("Invalid product data received");
            }
            return product;
        } catch (TimeoutException e) {
            throw new RuntimeException("Product service timeout - service unavailable");
        } catch (Exception e) {
            throw new RuntimeException("Critical product service failure", e);
        }
    }


    public JsonNode updateProduct(JsonNode product, Integer productId) {
        try {
            // Verify connection
            if (natsUtils.getConnection().getStatus() != Connection.Status.CONNECTED) {
                throw new RuntimeException("NATS connection is not established");
            }

            // Prepare headers
            Headers headers = new Headers();
            headers.add("Content-Type", "application/json");
            headers.add("Operation", "update");

            // Prepare payload
            ObjectNode payload = objectMapper.createObjectNode();
            payload.set("product", product);
            byte[] payloadBytes = objectMapper.writeValueAsBytes(payload);

            // Send request
            Message response = natsUtils.getConnection()
                    .request("product.update." + productId, headers, payloadBytes)
                    .get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            if (response == null) {
                throw new RuntimeException("No response received from NATS");
            }

            return parseResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update product", e);
        }
    }

    public JsonNode refreshCartState(Integer cartId) {
        try {
            Headers headers = createBasicHeaders();
            Message response = sendRequest(CART_REFRESH_PREFIX + cartId, headers);
            JsonNode responseNode = parseResponse(response);

            if (responseNode.has("error")) {
                throw new RuntimeException(responseNode.get("error").asText());
            }
            return responseNode;
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh cart state: " + e.getMessage(), e);
        }
    }

    public JsonNode removeAllByCartId(Integer cartId) {
        try {
            Headers headers = createBasicHeaders();
            Message response = sendRequest(CART_REMOVE_ITEMS_PREFIX + cartId, headers);
            JsonNode responseNode = parseResponse(response);

            if (responseNode.has("error")) {
                throw new RuntimeException(responseNode.get("error").asText());
            }
            return responseNode;
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove cart items: " + e.getMessage(), e);
        }
    }

    private Headers createAuthHeaders(String jwtToken) {
        Headers headers = new Headers();
        headers.add("Authorization", "Bearer " + jwtToken);
        headers.add("Content-Type", CONTENT_TYPE);
        return headers;
    }

    private Headers createBasicHeaders() {
        Headers headers = new Headers();
        headers.add("Content-Type", CONTENT_TYPE);
        return headers;
    }

    private Message sendRequest(String subject, Headers headers) throws Exception {

        try {

            return natsUtils.getConnection()
                    .request(subject, headers, new byte[0])
                    .get(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            System.out.println("ERROR: [NATS Request Failed] Subject: " + subject +
                    " Error: " + e.getMessage());
            throw e;
        }
    }


    private JsonNode parseResponse(Message msg) {
        if (msg == null) {
            throw new RuntimeException("No response received from NATS");
        }
        try {
            return objectMapper.readTree(msg.getData());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }
}