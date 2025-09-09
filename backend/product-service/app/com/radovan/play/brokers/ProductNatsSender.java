package com.radovan.play.brokers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.play.utils.NatsUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ProductNatsSender {

    private static final String ContentType = "application/json";
    private static final String CartItemsRefreshPrefix = "cart.updateAllByProductId.";
    private static final String CartItemsDeletePrefix = "cart.removeAllByProductId.";

    private  NatsUtils natsUtils;
    private  ObjectMapper objectMapper;

    @Inject
    private void initialize(NatsUtils natsUtils, ObjectMapper objectMapper) {
        this.natsUtils = natsUtils;
        this.objectMapper = objectMapper;
    }

    public void sendCartUpdateRequest(Integer productId, String jwtToken) {
        try {
            ObjectNode messagePayload = objectMapper.createObjectNode();
            messagePayload.put("Product-ID", productId);
            messagePayload.put("Authorization", jwtToken);

            String subject = CartItemsRefreshPrefix + productId;
            byte[] payload = objectMapper.writeValueAsBytes(messagePayload);

            natsUtils.getConnection().publish(subject, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCartDeleteRequest(Integer productId, String jwtToken) {
        try {
            ObjectNode messagePayload = objectMapper.createObjectNode();
            messagePayload.put("Product-ID", productId);
            messagePayload.put("Authorization", jwtToken);

            String subject = CartItemsDeletePrefix + productId;
            byte[] payload = objectMapper.writeValueAsBytes(messagePayload);

            natsUtils.getConnection().publish(subject, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}