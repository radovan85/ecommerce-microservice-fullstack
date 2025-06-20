package com.radovan.play.brokers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.play.utils.NatsUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;


@Singleton
public class ProductNatsSender {

    private static final String CONTENT_TYPE = "application/json";
    private static final String CART_ITEMS_REFRESH_PREFIX = "cart.updateAllByProductId.";
    private static final String CART_ITEMS_delete_PREFIX = "cart.removeAllByProductId.";

    private final NatsUtils natsUtils;
    private final ObjectMapper objectMapper;

    @Inject
    public ProductNatsSender(NatsUtils natsUtils, ObjectMapper objectMapper) {
        this.natsUtils = natsUtils;
        this.objectMapper = objectMapper;
    }

    public void sendCartUpdateRequest(Integer productId, String jwtToken) {
        try {
            ObjectNode messagePayload = objectMapper.createObjectNode();
            messagePayload.put("Product-ID", productId);
            messagePayload.put("Authorization", jwtToken);

            natsUtils.getConnection().publish(CART_ITEMS_REFRESH_PREFIX + productId, objectMapper.writeValueAsBytes(messagePayload));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCartDeleteRequest(Integer productId) {
        try {
            natsUtils.getConnection().publish(CART_ITEMS_delete_PREFIX + productId, new byte[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
