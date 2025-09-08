package com.radovan.play.brokers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.play.services.OrderService;
import com.radovan.play.utils.NatsUtils;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import io.nats.client.impl.Headers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class OrderNatsListener {

    private NatsUtils natsUtils;
    private ObjectMapper objectMapper;
    private OrderService orderService;

    @Inject
    private void initialize(NatsUtils natsUtils, ObjectMapper objectMapper, OrderService orderService) {
        this.natsUtils = natsUtils;
        this.objectMapper = objectMapper;
        this.orderService = orderService;
        initListeners();
    }


    private void initListeners() {
        Connection connection = natsUtils.getConnection();
        if (connection != null) {
            Dispatcher dispatcher = connection.createDispatcher();
            dispatcher.subscribe("order.deleteAll.*", onOrdersDelete);
        } else {
            System.err.println("*** NATS connection unavailable — order listener not initialized");
        }
    }

    private final MessageHandler onOrdersDelete = (Message msg) -> {
        String subject = msg.getSubject();
        String replyTo = msg.getReplyTo();
        int cartId = extractIdFromSubject(subject, "order.deleteAll.");

        try {
            orderService.deleteAllByCartId(cartId);

            ObjectNode responseNode = objectMapper.createObjectNode();
            responseNode.put("status", 200);
            responseNode.put("message", "All orders for cart ID " + cartId + " deleted successfully");

            publishResponse(replyTo, responseNode);
        } catch (Exception e) {
            sendErrorResponse(replyTo, "Unexpected error while deleting orders", 500);
        }
    };

    private int extractIdFromSubject(String subject, String prefix) {
        try {
            return Integer.parseInt(subject.replace(prefix, ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void publishResponse(String replyTo, ObjectNode node) {
        if (replyTo != null && !replyTo.isEmpty()) {
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(node);
                natsUtils.getConnection().publish(replyTo, bytes);
            } catch (Exception e) {
                System.err.println("Failed to publish response: " + e.getMessage());
            }
        }
    }

    private void sendErrorResponse(String replyTo, String message, int status) {
        try {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("status", status);
            errorNode.put("message", message);

            Headers headers = new Headers();
            headers.add("Content-Type", "application/json");

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