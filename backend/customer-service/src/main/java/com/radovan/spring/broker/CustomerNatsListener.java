package com.radovan.spring.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.spring.dto.CustomerDto;
import com.radovan.spring.dto.ShippingAddressDto;
import com.radovan.spring.services.CustomerService;
import com.radovan.spring.services.ShippingAddressService;
import com.radovan.spring.utils.NatsUtils;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomerNatsListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomerNatsListener.class);

    private NatsUtils natsUtils;
    private ObjectMapper objectMapper;
    private CustomerService customerService;
    private ShippingAddressService addressService;


    @Autowired
    private void initialize(NatsUtils natsUtils, ObjectMapper objectMapper,
                            CustomerService customerService, ShippingAddressService addressService) {
        this.natsUtils = natsUtils;
        this.objectMapper = objectMapper;
        this.customerService = customerService;
        this.addressService = addressService;
        init();
    }

    public void init() {
        Dispatcher dispatcher = natsUtils.getConnection().createDispatcher();
        dispatcher.subscribe("customer.getCurrent", onGetCurrentCustomer);
        dispatcher.subscribe("address.getAddress.*", getAddressNode);
        dispatcher.subscribe("address.update.*", onAddressUpdate);
    }

    private final MessageHandler onGetCurrentCustomer = (Message msg) -> {
        try {
            JsonNode payload = objectMapper.readTree(msg.getData());
            String jwtToken = Optional.ofNullable(payload.get("token"))
                    .map(JsonNode::asText)
                    .orElseThrow(() -> new RuntimeException("Missing token in customer.getCurrent payload"));

            CustomerDto currentCustomer = customerService.getCurrentCustomer(jwtToken);
            String replyTo = Optional.ofNullable(msg.getReplyTo()).orElse("customer.response");

            publishSafely(replyTo, objectMapper.writeValueAsBytes(currentCustomer));

        } catch (Exception ex) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("status", 500);
            errorNode.put("error", "Failed to retrieve current customer: " + ex.getMessage());
            String replyTo = Optional.ofNullable(msg.getReplyTo()).orElse("customer.response");
            publishSafely(replyTo, safeBytes(errorNode));
        }
    };

    private final MessageHandler getAddressNode = (Message msg) -> {
        try {
            JsonNode payload = objectMapper.readTree(msg.getData());
            String jwtToken = Optional.ofNullable(payload.get("token"))
                    .map(JsonNode::asText)
                    .orElseThrow(() -> new RuntimeException("Missing token in address.getAddress payload"));

            int addressId = extractIdFromSubject(msg.getSubject(), "address.getAddress.");
            ShippingAddressDto address = addressService.getAddressById(addressId);
            String replyTo = Optional.ofNullable(msg.getReplyTo()).orElse("address.response");

            publishSafely(replyTo, objectMapper.writeValueAsBytes(address));

        } catch (Exception ex) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("status", 500);
            errorNode.put("error", "Failed to retrieve address: " + ex.getMessage());
            String replyTo = Optional.ofNullable(msg.getReplyTo()).orElse("address.response");
            publishSafely(replyTo, safeBytes(errorNode));
        }
    };

    private final MessageHandler onAddressUpdate = (Message msg) -> {
        try {
            int addressId = extractIdFromSubject(msg.getSubject(), "address.update.");
            JsonNode payload = objectMapper.readTree(msg.getData());

            String jwtToken = Optional.ofNullable(payload.get("Authorization"))
                    .map(JsonNode::asText)
                    .orElseThrow(() -> new RuntimeException("Missing Authorization token in address.update payload"));

            JsonNode addressNode = Optional.ofNullable(payload.get("address"))
                    .orElseThrow(() -> new RuntimeException("Missing address node in payload"));

            ShippingAddressDto dto = objectMapper.treeToValue(addressNode, ShippingAddressDto.class);
            dto.setShippingAddressId(addressId);

            ShippingAddressDto updatedAddress = addressService.updateAddress(dto, addressId);

            ObjectNode responseNode = objectMapper.createObjectNode();
            responseNode.put("status", 200);
            responseNode.set("address", objectMapper.valueToTree(updatedAddress));

            String replyTo = Optional.ofNullable(msg.getReplyTo()).orElse("address.response");
            publishSafely(replyTo, safeBytes(responseNode));

        } catch (Exception ex) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("status", 500);
            errorNode.put("message", "Address update failed: " + ex.getMessage());
            String replyTo = Optional.ofNullable(msg.getReplyTo()).orElse("address.response");
            publishSafely(replyTo, safeBytes(errorNode));
        }
    };

    private int extractIdFromSubject(String subject, String prefix) {
        try {
            return Integer.parseInt(subject.replace(prefix, ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ✅ Helper za sigurno publish-ovanje
    private void publishSafely(String subject, byte[] payload) {
        try {
            natsUtils.getConnection().publish(subject, payload);
        } catch (Exception e) {
            logger.error("❌ Failed to publish to {}: {}", subject, e.getMessage());
        }
    }

    // ✅ Helper za sigurno pretvaranje u byte[]
    private byte[] safeBytes(ObjectNode node) {
        try {
            return objectMapper.writeValueAsBytes(node);
        } catch (JsonProcessingException e) {
            ObjectNode fallback = objectMapper.createObjectNode();
            fallback.put("status", 500);
            fallback.put("error", "Serialization error: " + e.getMessage());
            try {
                return objectMapper.writeValueAsBytes(fallback);
            } catch (JsonProcessingException ex) {
                return "{\"status\":500,\"error\":\"Unrecoverable serialization error\"}".getBytes();
            }
        }
    }
}
