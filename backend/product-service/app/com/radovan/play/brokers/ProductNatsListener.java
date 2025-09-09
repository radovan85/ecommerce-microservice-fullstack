package com.radovan.play.brokers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.play.dto.ProductDto;
import com.radovan.play.services.ProductService;
import com.radovan.play.utils.NatsUtils;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import io.nats.client.impl.Headers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class ProductNatsListener {

    private static final String ProductIdHeader = "Product-ID";
    private static final String ContentTypeHeader = "Content-Type";
    private static final String ApplicationJson = "application/json";
    private static final String ProductUpdatePrefix = "product.update.";
    private static final String ProductGetPrefix = "product.get.";

    private  ProductService productService;
    private  NatsUtils natsUtils;
    private  ObjectMapper objectMapper;

    @Inject
    private void initialize(ProductService productService, NatsUtils natsUtils, ObjectMapper objectMapper) {
        this.productService = productService;
        this.natsUtils = natsUtils;
        this.objectMapper = objectMapper;
        initListeners();
    }


    private void initListeners() {
        try {
            Connection connection = natsUtils.getConnection();
            MessageHandler messageHandler = this::handleMessage;
            Dispatcher dispatcher = connection.createDispatcher(messageHandler);

            dispatcher.subscribe("product.update.*");
            dispatcher.subscribe("product.get.*");
        } catch (Exception e) {
            System.err.println("PRODUCT SERVICE INIT ERROR: " + e.getMessage());
            throw new RuntimeException("NATS initialization failed", e);
        }
    }

    private void handleMessage(Message msg) {
        try {
            String subject = msg.getSubject();
            if (subject.startsWith(ProductUpdatePrefix)) {
                handleUpdateRequest(msg);
            } else if (subject.startsWith(ProductGetPrefix)) {
                handleGetRequest(msg);
            }
        } catch (Exception e) {
            System.err.println("PRODUCT SERVICE ERROR: " + e.getMessage());
            sendErrorResponse(msg.getReplyTo(), "Internal server error", 500);
        }
    }

    private void handleGetRequest(Message msg) {
        int productId = extractIdFromSubject(msg.getSubject(), ProductGetPrefix);
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
            int productId = extractIdFromSubject(msg.getSubject(), ProductUpdatePrefix);
            JsonNode payload = objectMapper.readTree(msg.getData());

            if (!payload.has("product")) {
                throw new RuntimeException("Missing 'product' field");
            }
            if (!payload.has("Authorization")) {
                throw new RuntimeException("Missing Authorization token");
            }

            String jwtToken = payload.get("Authorization").asText();
            ProductDto productDto = objectMapper.treeToValue(payload.get("product"), ProductDto.class);

            ProductDto updatedProduct = productService.updateProduct(productDto, productId, jwtToken);

            ObjectNode response = objectMapper.createObjectNode();
            response.set("product", objectMapper.valueToTree(updatedProduct));
            natsUtils.getConnection().publish(msg.getReplyTo(), objectMapper.writeValueAsBytes(response));

        } catch (Exception ex) {
            sendErrorResponse(msg.getReplyTo(), "Update failed: " + ex.getMessage(), 500);
        }
    }

    private int extractIdFromSubject(String subject, String prefix) {
        try {
            return Integer.parseInt(subject.replace(prefix, ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void sendErrorResponse(String replyTo, String message, int status) {
        try {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("status", status);
            errorNode.put("message", message);

            Headers headers = new Headers();
            headers.add(ContentTypeHeader, ApplicationJson);

            System.out.println("ERROR: " + message);
            natsUtils.getConnection().publish(replyTo, headers, objectMapper.writeValueAsBytes(errorNode));
        } catch (Exception e) {
            try {
                natsUtils.getConnection().publish(replyTo, new byte[0]);
            } catch (Exception ex) {
                System.err.println("Failed to send error response: " + ex.getMessage());
            }
        }
    }
}