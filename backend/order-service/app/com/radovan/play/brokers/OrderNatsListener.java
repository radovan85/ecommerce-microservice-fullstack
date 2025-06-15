package com.radovan.play.brokers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.play.services.OrderService;
import com.radovan.play.utils.NatsUtils;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class OrderNatsListener {

    private static final String ORDER_DELETE_ALL_BY_CART_PREFIX = "order.deleteAllByCartId.";
    private static final int HTTP_SUCCESS = 200;
    private static final int HTTP_NOT_ACCEPTABLE = 406;
    private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

    private final NatsUtils natsUtils;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Inject
    public OrderNatsListener(NatsUtils natsUtils, OrderService orderService, ObjectMapper objectMapper) {
        this.natsUtils = natsUtils;
        this.orderService = orderService;
        this.objectMapper = objectMapper;
        initializeListeners();
    }

    @Inject
    private void initializeListeners() {
        Dispatcher dispatcher = natsUtils.getConnection().createDispatcher(this::handleMessage);
        dispatcher.subscribe(ORDER_DELETE_ALL_BY_CART_PREFIX + "*");
    }


    private void handleMessage(Message msg) {
        try {
            System.out.println("DEBUG: Received message in OrderNatsListener");
            System.out.println("DEBUG: Message Subject: " + msg.getSubject());

            if (msg.getSubject().startsWith(ORDER_DELETE_ALL_BY_CART_PREFIX)) {
                handleDeleteAllByCartId(msg);
            } else {
                System.out.println("DEBUG: Ignoring unrelated message: " + msg.getSubject());
            }
        } catch (Exception e) {
            sendErrorResponse(msg.getReplyTo(), "Error processing request: " + e.getMessage(), HTTP_INTERNAL_SERVER_ERROR);
        }
    }


    private void handleDeleteAllByCartId(Message msg) {
        try {


            String authorizationHeader = msg.getHeaders() != null ? msg.getHeaders().getFirst("Authorization") : "MISSING";


            if ("MISSING".equals(authorizationHeader)) {
                throw new RuntimeException("Authorization header missing in OrderNatsListener!");
            }

            Integer cartId = extractCartIdFromSubject(msg.getSubject());

            orderService.deleteAllByCartId(cartId);
            sendSuccessResponse(msg.getReplyTo(), "All orders for cart " + cartId + " deleted successfully");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to process delete request - " + e.getMessage());
            sendErrorResponse(msg.getReplyTo(), "Failed to delete orders by cart ID: " + e.getMessage(), HTTP_INTERNAL_SERVER_ERROR);
        }
    }


    private Integer extractCartIdFromSubject(String subject) {
        return Integer.parseInt(subject.replace(ORDER_DELETE_ALL_BY_CART_PREFIX, ""));
    }

    private void sendSuccessResponse(String replyTo, String message) {
        try {
            if (replyTo != null) {
                ObjectNode responseNode = objectMapper.createObjectNode();
                responseNode.put("status", HTTP_SUCCESS);
                responseNode.put("message", message);
                natsUtils.getConnection().publish(replyTo, objectMapper.writeValueAsBytes(responseNode));
            }
        } catch (Exception e) {
            // Log error if needed
        }
    }

    private void sendErrorResponse(String replyTo, String errorMessage, int statusCode) {
        try {
            if (replyTo != null) {
                ObjectNode errorNode = objectMapper.createObjectNode();
                errorNode.put("error", errorMessage);
                errorNode.put("status", statusCode);
                errorNode.put("message", errorMessage);
                natsUtils.getConnection().publish(replyTo, objectMapper.writeValueAsBytes(errorNode));
            }
        } catch (Exception ignored) {
            // Log error if needed
        }
    }
}