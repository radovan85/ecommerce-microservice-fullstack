package com.radovan.spring.broker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.spring.exceptions.ExistingInstanceException;
import com.radovan.spring.utils.NatsUtils;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.time.Duration;

@Component
public class CustomerNatsSender {

    private static final String USER_RESPONSE_QUEUE = "user.response";

    private  NatsUtils natsUtils;
    private  ObjectMapper objectMapper;

    @Autowired
    private void initialize(NatsUtils natsUtils, ObjectMapper objectMapper) {
        this.natsUtils = natsUtils;
        this.objectMapper = objectMapper;
    }

    public int sendUserCreate(JsonNode userPayload) throws ExistingInstanceException, Exception {
        try {
            byte[] payloadBytes = objectMapper.writeValueAsBytes(userPayload);
            Message reply = natsUtils.getConnection()
                    .request("user.create", payloadBytes, Duration.ofSeconds(5));

            JsonNode response = objectMapper.readTree(reply.getData());
            int status = response.has("status") ? response.get("status").asInt() : 500;

            if (status == 200 && response.has("id")) {
                return response.get("id").asInt();
            } else if (status == 409) {
                throw new ExistingInstanceException(new Error("Email already exists."));
            } else {
                String msg = response.has("message") ? response.get("message").asText() : "Unknown error.";
                throw new Exception("User creation failed: " + msg);
            }
        } catch (ExistingInstanceException e) {
            throw e;
        } catch (Exception ex) {
            throw new Exception("NATS user.create failed: " + ex.getMessage(), ex);
        }
    }

    public void sendDeleteUserEvent(int userId, String jwtToken) {
        sendUserEvent("user.delete." + userId, userId, jwtToken);
    }

    public void sendSuspendUserEvent(int userId, String jwtToken) {
        sendUserEvent("user.suspend." + userId, userId, jwtToken);
    }

    public void sendReactivateUserEvent(int userId, String jwtToken) {
        sendUserEvent("user.reactivate." + userId, userId, jwtToken);
    }

    public JsonNode retrieveCurrentUser(String jwtToken) throws Exception {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("token", jwtToken);
            byte[] payloadBytes = objectMapper.writeValueAsBytes(payload);

            Message reply = natsUtils.getConnection()
                    .request("user.get", payloadBytes, Duration.ofSeconds(2));

            if (reply == null || reply.getData() == null) {
                throw new RuntimeException("No reply received from user.get");
            }

            JsonNode response = objectMapper.readTree(reply.getData());
            int status = response.has("status") ? response.get("status").asInt() : 200;

            if (status >= 400) {
                String msg = response.has("message") ? response.get("message").asText() : "Unknown error.";
                throw new RuntimeException("Failed to fetch current user: " + msg);
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving current user: " + e.getMessage(), e);
        }
    }

    public void sendDeleteAllOrders(int cartId, String jwtToken) throws Exception {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("Authorization", jwtToken);
            byte[] payloadBytes = objectMapper.writeValueAsBytes(payload);

            Message reply = natsUtils.getConnection()
                    .request("order.deleteAll." + cartId, payloadBytes, Duration.ofSeconds(2));

            if (reply == null || reply.getData() == null) {
                throw new RuntimeException("No reply received from order.deleteAll");
            }

            JsonNode response = objectMapper.readTree(reply.getData());
            int status = response.has("status") ? response.get("status").asInt() : 500;

            if (status != 200) {
                String msg = response.has("message") ? response.get("message").asText() : "Unknown error.";
                throw new RuntimeException("Order deletion failed: " + msg);
            }
        } catch (Exception ex) {
            throw new RuntimeException("NATS order.deleteAll failed: " + ex.getMessage(), ex);
        }
    }

    private void sendUserEvent(String subject, int userId, String jwtToken) {
        try {
            byte[] payload = createUserEventPayload(userId);
            Headers headers = createAuthorizationHeaders(jwtToken);
            natsUtils.getConnection().publish(subject, headers, payload);
        } catch (Exception e) {
            throw new RuntimeException("Error sending user event: " + subject, e);
        }
    }

    public void sendCartDelete(int cartId, String jwtToken) throws Exception {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("Authorization", jwtToken);
            byte[] payloadBytes = objectMapper.writeValueAsBytes(payload);

            Message reply = natsUtils.getConnection()
                    .request("cart.delete." + cartId, payloadBytes, Duration.ofSeconds(2));

            if (reply == null || reply.getData() == null) {
                throw new RuntimeException("No reply received from cart.delete");
            }

            JsonNode response = objectMapper.readTree(reply.getData());
            int status = response.has("status") ? response.get("status").asInt() : 500;

            if (status != 200) {
                String msg = response.has("message") ? response.get("message").asText() : "Unknown error.";
                throw new RuntimeException("Cart deletion failed: " + msg);
            }
        } catch (Exception ex) {
            throw new RuntimeException("NATS cart.delete failed: " + ex.getMessage(), ex);
        }
    }

    private byte[] createUserEventPayload(int userId) {
        try {
            ObjectNode nodeRequest = objectMapper.createObjectNode();
            nodeRequest.put("userId", userId);
            return objectMapper.writeValueAsBytes(nodeRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user event payload", e);
        }
    }

    private Headers createAuthorizationHeaders(String jwtToken) {
        Headers headers = new Headers();
        headers.add("Nats-Reply-To", USER_RESPONSE_QUEUE);
        headers.add("Authorization", "Bearer " + jwtToken);
        return headers;
    }

    public int sendCartCreate() throws Exception {
        try {
            ObjectNode emptyPayload = objectMapper.createObjectNode();
            byte[] payloadBytes = objectMapper.writeValueAsBytes(emptyPayload);

            Message reply = natsUtils.getConnection()
                    .request("cart.create", payloadBytes, Duration.ofSeconds(2));

            if (reply == null || reply.getData() == null) {
                throw new RuntimeException("No reply received from cart.create");
            }

            JsonNode response = objectMapper.readTree(reply.getData());
            int status = response.has("status") ? response.get("status").asInt() : 500;

            if (status == 200 && response.has("id")) {
                return response.get("id").asInt();
            } else {
                String msg = response.has("message") ? response.get("message").asText() : "Unknown error.";
                throw new Exception("Cart creation failed: " + msg);
            }
        } catch (Exception ex) {
            throw new Exception("NATS cart.create failed: " + ex.getMessage(), ex);
        }
    }
}