package com.radovan.play.brokers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.play.dto.ProductDto;
import com.radovan.play.exceptions.InstanceUndefinedException;
import com.radovan.play.services.ProductService;
import com.radovan.play.utils.NatsUtils;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ProductNatsListener {

    private static final String PRODUCT_ID_HEADER = "Product-ID";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String PRODUCT_UPDATE_PREFIX = "product.update.";
    private static final String PRODUCT_GET_PREFIX = "product.get.";

    private final NatsUtils natsUtils;
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @Inject
    public ProductNatsListener(ProductService productService, NatsUtils natsUtils, ObjectMapper objectMapper) {
        this.productService = productService;
        this.natsUtils = natsUtils;
        this.objectMapper = objectMapper;
    }

    @Inject
    private void initListeners() {
        try {
            Connection connection = natsUtils.getConnection();

            Dispatcher dispatcher = connection.createDispatcher(this::handleMessage);

            // Subscribe using explicit pattern matching
            dispatcher.subscribe("product.update.*");
            dispatcher.subscribe("product.get.*");

            // Add a test message to verify subscription
            connection.publish("product.update.test", "TEST".getBytes());

        } catch (Exception e) {
            System.err.println("PRODUCT SERVICE INIT ERROR: " + e.getMessage());
            throw new RuntimeException("NATS initialization failed", e);
        }
    }


    private void handleMessage(Message msg) {

        try {
            if (msg.getSubject().startsWith("product.update.")) {
                handleUpdateRequest(msg);
            } else if (msg.getSubject().startsWith("product.get.")) {
                handleGetRequest(msg);
            }
        } catch (Exception e) {
            System.err.println("PRODUCT SERVICE ERROR: " + e.getMessage());
            sendErrorResponse(msg.getReplyTo(), "Internal server error", 500);
        }
    }


    private void handleGetRequest(Message msg) {
        Integer productId = extractIdFromSubject(msg.getSubject(), "product.get.");

        try {
            ProductDto product = productService.getProductById(productId);
            ObjectNode response = objectMapper.createObjectNode();
            response.set("product", objectMapper.valueToTree(product));
            natsUtils.getConnection().publish(msg.getReplyTo(), objectMapper.writeValueAsBytes(response));
        } catch (Exception e) {
            sendErrorResponse(msg.getReplyTo(), "Failed to retrieve product", 500);
        }
    }

     





    private void handleUpdateRequest(Message msg) {
        try {
            Integer productId = extractIdFromSubject(msg.getSubject(), "product.update.");
            JsonNode payload = objectMapper.readTree(msg.getData());

            if (!payload.has("product")) {
                throw new RuntimeException("Invalid payload format");
            }

            ProductDto productDto = objectMapper.treeToValue(payload.get("product"), ProductDto.class);
            ProductDto updatedProduct = productService.updateProduct(productDto, productId);

            ObjectNode response = objectMapper.createObjectNode();
            response.set("product", objectMapper.valueToTree(updatedProduct));
            natsUtils.getConnection().publish(msg.getReplyTo(), objectMapper.writeValueAsBytes(response));

        } catch (Exception e) {
            sendErrorResponse(msg.getReplyTo(), "Update failed", 500);
        }
    }


    private Integer extractProductId(Message msg) {
        Headers headers = msg.getHeaders();
        if (headers == null || headers.getFirst(PRODUCT_ID_HEADER) == null) {
            return null;
        }

        try {
            Integer productId = Integer.parseInt(headers.getFirst(PRODUCT_ID_HEADER));
            return productId;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer extractIdFromSubject(String subject, String prefix) {
        try {
            return Integer.parseInt(subject.replace(prefix, ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void processProductRequest(String replyTo, Integer productId) {
        try {
            ProductDto product = productService.getProductById(productId);
            sendSuccessResponse(replyTo, product);
        } catch (InstanceUndefinedException e) {
            sendErrorResponse(replyTo, e.getMessage(), 422);
        } catch (Exception e) {
            sendErrorResponse(replyTo, "Error retrieving product", 500);
        }
    }

    private void sendSuccessResponse(String replyTo, ProductDto product) {
        try {
            JsonNode productJson = objectMapper.valueToTree(product);
            System.out.println("DEBUG: Sending success response: " + productJson);

            Headers headers = new Headers();
            headers.add(CONTENT_TYPE_HEADER, APPLICATION_JSON);

            natsUtils.getConnection().publish(replyTo, headers, objectMapper.writeValueAsBytes(productJson));
        } catch (Exception e) {
            sendErrorResponse(replyTo, "Error formatting response", 500);
        }
    }

    private void sendErrorResponse(String replyTo, String message, int status) {
        try {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("status", status);
            errorNode.put("message", message);

            System.out.println("ERROR: Sending error response: " + errorNode);

            Headers headers = new Headers();
            headers.add(CONTENT_TYPE_HEADER, APPLICATION_JSON);

            natsUtils.getConnection().publish(replyTo, headers, objectMapper.writeValueAsBytes(errorNode));
        } catch (Exception e) {
            natsUtils.getConnection().publish(replyTo, new byte[0]);
        }
    }
}
