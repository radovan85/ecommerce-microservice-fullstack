package com.radovan.spring.broker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.radovan.spring.entity.CartEntity;
import com.radovan.spring.exceptions.InvalidCartException;
import com.radovan.spring.repositories.CartRepository;
import com.radovan.spring.services.CartItemService;
import com.radovan.spring.services.CartService;
import com.radovan.spring.utils.NatsUtils;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.time.Instant;
import java.util.Optional;

@Component
public class CartNatsListener {

    private  NatsUtils natsUtils;
    private  CartRepository cartRepository;
    private  ObjectMapper objectMapper;
    private  CartItemService cartItemService;
    private  CartService cartService;

    @Autowired
    private void initialize(NatsUtils natsUtils, CartRepository cartRepository,
                            ObjectMapper objectMapper, CartItemService cartItemService,
                            CartService cartService) {
        this.natsUtils = natsUtils;
        this.cartRepository = cartRepository;
        this.objectMapper = objectMapper;
        this.cartItemService = cartItemService;
        this.cartService = cartService;
        initListeners();
    }


    public void initListeners() {
        Connection connection = natsUtils.getConnection();
        if (connection != null) {
            Dispatcher dispatcher = connection.createDispatcher();
            dispatcher.subscribe("cart.create", onCartCreate);
            dispatcher.subscribe("cart.updateAllByProductId.*", onCartUpdate);
            dispatcher.subscribe("cart.delete.*", onCartDelete);
            dispatcher.subscribe("cart.removeAllByProductId.*", onProductDelete);
            dispatcher.subscribe("cart.validate.*", onCartValidate);
            dispatcher.subscribe("cart.getItems.*", getCartItems);
            dispatcher.subscribe("cart.removeAllByCartId.*", onCartClearById);
            dispatcher.subscribe("cart.refreshState.*", onCartRefreshState);
        } else {
            System.err.println("*** NATS connection unavailable — cart.create listener not initialized");
        }
    }

    private final MessageHandler onCartCreate = (Message msg) -> {
        try {
            CartEntity newCart = new CartEntity();
            newCart.setCartPrice(0f);
            CartEntity savedCart = cartRepository.save(newCart);

            ObjectNode response = objectMapper.createObjectNode();
            response.put("status", 200);
            response.put("id", savedCart.getCartId());

            String replyTo = msg.getReplyTo();
            if (replyTo != null) {
                natsUtils.getConnection().publish(replyTo, safeBytes(response));
            }

        } catch (Exception ex) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("status", 500);
            error.put("message", "Cart creation failed: " + ex.getMessage());

            String replyTo = msg.getReplyTo();
            if (replyTo != null) {
                natsUtils.getConnection().publish(replyTo, safeBytes(error));
            }
        }
    };


    private final MessageHandler onCartUpdate = (Message msg) -> {
        try {
            JsonNode payload = objectMapper.readTree(msg.getData());
            int productId = extractIdFromSubject(msg.getSubject(), "cart.updateAllByProductId.");
            String jwtToken = Optional.ofNullable(payload.get("Authorization"))
                    .map(JsonNode::asText)
                    .orElse("");

            cartItemService.updateAllByProductId(productId, jwtToken);

            ObjectNode response = objectMapper.createObjectNode();
            response.put("status", 200);
            response.put("message", "Cart items updated");
            publishResponse(msg.getReplyTo(), response);
        } catch (Exception ex) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("status", 500);
            error.put("message", "Update failed: " + ex.getMessage());
            publishResponse(msg.getReplyTo(), error);
        }
    };

    private final MessageHandler onCartDelete = (Message msg) -> {
        try {
            JsonNode payload = objectMapper.readTree(msg.getData());
            int cartId = extractIdFromSubject(msg.getSubject(), "cart.delete.");
            String jwtToken = Optional.ofNullable(payload.get("Authorization"))
                    .map(JsonNode::asText)
                    .orElse("");

            cartRepository.deleteById(cartId);

            ObjectNode response = objectMapper.createObjectNode();
            response.put("status", 200);
            response.put("message", "Cart deleted");
            publishResponse(msg.getReplyTo(), response);
        } catch (Exception ex) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("status", 500);
            error.put("message", "Delete failed: " + ex.getMessage());
            publishResponse(msg.getReplyTo(), error);
        }
    };

    private final MessageHandler onCartValidate = (Message msg) -> {
        try {
            int cartId = extractIdFromSubject(msg.getSubject(), "cart.validate.");
            JsonNode payload = objectMapper.readTree(msg.getData());
            String jwtToken = Optional.ofNullable(payload.get("Authorization"))
                    .map(JsonNode::asText)
                    .orElse("");

            Object cartDto = cartService.validateCart(cartId);
            JsonNode responseJson = objectMapper.valueToTree(cartDto);

            publishResponse(msg.getReplyTo(), (ObjectNode) responseJson);
        } catch (InvalidCartException ex) {
            System.out.println("[DEBUG] Caught InvalidCartException directly");
            ObjectNode error = objectMapper.createObjectNode();
            error.put("status", 406);
            error.put("message", "Validation failed: " + ex.getMessage());
            publishResponse(msg.getReplyTo(), error);
        } catch (Exception ex) {
            System.out.println("[DEBUG] Caught generic exception: " + ex.getClass().getName());
            Throwable cause = ex.getCause();
            boolean isInvalidCart = (cause != null && cause instanceof InvalidCartException);
            ObjectNode error = objectMapper.createObjectNode();
            error.put("status", isInvalidCart ? 406 : 500);
            error.put("message", "Validation failed: " + ex.getMessage());
            publishResponse(msg.getReplyTo(), error);
        }
    };

    private final MessageHandler getCartItems = (Message msg) -> {
        try {
            int cartId = extractIdFromSubject(msg.getSubject(), "cart.getItems.");
            JsonNode payload = objectMapper.readTree(msg.getData());
            String jwtToken = Optional.ofNullable(payload.get("Authorization"))
                    .map(JsonNode::asText)
                    .orElse("");

            Iterable<?> cartItemDtos = cartItemService.listAllByCartId(cartId);

            ArrayNode itemsArrayNode = objectMapper.createArrayNode();
            for (Object dto : cartItemDtos) {
                JsonNode jsonNode = objectMapper.valueToTree(dto);
                itemsArrayNode.add(jsonNode);
            }

            ObjectNode responseJson = objectMapper.createObjectNode();
            responseJson.set("items", itemsArrayNode);
            responseJson.put("status", 200);
            responseJson.put("cartId", cartId);
            responseJson.put("timestamp", Instant.now().toString());

            publishResponse(msg.getReplyTo(), responseJson);
        } catch (Exception ex) {
            ObjectNode errorJson = objectMapper.createObjectNode();
            errorJson.put("status", 500);
            errorJson.put("message", "Error retrieving items: " + ex.getMessage());
            publishResponse(msg.getReplyTo(), errorJson);
        }
    };

    private final MessageHandler onProductDelete = (Message msg) -> {
        try {
            int productId = extractIdFromSubject(msg.getSubject(), "cart.removeAllByProductId.");
            JsonNode payload = objectMapper.readTree(msg.getData());
            String jwtToken = Optional.ofNullable(payload.get("Authorization"))
                    .map(JsonNode::asText)
                    .orElse("");

            cartItemService.removeAllByProductId(productId);

            ObjectNode response = objectMapper.createObjectNode();
            response.put("status", 200);
            response.put("message", "Product removed from carts");
            publishResponse(msg.getReplyTo(), response);
        } catch (Exception ex) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("status", 500);
            error.put("message", "Remove failed: " + ex.getMessage());
            publishResponse(msg.getReplyTo(), error);
        }
    };

    private final MessageHandler onCartClearById = (Message msg) -> {
        try {
            int cartId = extractIdFromSubject(msg.getSubject(), "cart.removeAllByCartId.");
            JsonNode payload = objectMapper.readTree(msg.getData());
            String jwtToken = Optional.ofNullable(payload.get("Authorization"))
                    .map(JsonNode::asText)
                    .orElse("");

            cartItemService.removeAllByCartId(cartId);

            ObjectNode response = objectMapper.createObjectNode();
            response.put("status", 200);
            response.put("message", "All items removed from cart " + cartId);
            publishResponse(msg.getReplyTo(), response);
        } catch (Exception ex) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("status", 500);
            error.put("message", "Failed to clear cart: " + ex.getMessage());
            publishResponse(msg.getReplyTo(), error);
        }
    };

    private final MessageHandler onCartRefreshState = (Message msg) -> {
        try {
            int cartId = extractIdFromSubject(msg.getSubject(), "cart.refreshState.");
            JsonNode payload = objectMapper.readTree(msg.getData());
            String jwtToken = Optional.ofNullable(payload.get("Authorization"))
                    .map(JsonNode::asText)
                    .orElse("");

            cartService.refreshCartState(cartId);

            ObjectNode response = objectMapper.createObjectNode();
            response.put("status", 200);
            response.put("message", "Cart state refreshed for cart " + cartId);
            publishResponse(msg.getReplyTo(), response);
        } catch (Exception ex) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("status", 500);
            error.put("message", "Failed to refresh cart state: " + ex.getMessage());
            publishResponse(msg.getReplyTo(), error);
        }
    };

    private void publishResponse(String replyTo, ObjectNode node) {
        if (replyTo != null && !replyTo.isEmpty()) {
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(node);
                natsUtils.getConnection().publish(replyTo, bytes);
            } catch (Exception ex) {
                System.err.println("Failed to publish response: " + ex.getMessage());
            }
        }
    }

    private int extractIdFromSubject(String subject, String prefix) {
        String suffix = subject.replace(prefix, "");
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID in subject: " + subject);
        }
    }

    private byte[] safeBytes(ObjectNode node) {
        try {
            return objectMapper.writeValueAsBytes(node);
        } catch (Exception e) {
            ObjectNode fallback = objectMapper.createObjectNode();
            fallback.put("status", 500);
            fallback.put("message", "Serialization error: " + e.getMessage());
            try {
                return objectMapper.writeValueAsBytes(fallback);
            } catch (Exception ex) {
                return "{\"status\":500,\"message\":\"Unrecoverable serialization error\"}".getBytes();
            }
        }
    }

}