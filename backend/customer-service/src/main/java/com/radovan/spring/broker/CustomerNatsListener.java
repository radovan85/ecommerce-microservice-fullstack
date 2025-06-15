package com.radovan.spring.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.spring.dto.CustomerDto;
import com.radovan.spring.dto.ShippingAddressDto;
import com.radovan.spring.services.CustomerService;
import com.radovan.spring.services.ShippingAddressService;
import com.radovan.spring.utils.JwtUtil;
import com.radovan.spring.utils.NatsUtils;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerNatsListener {

    private static final String SHIPPING_ADDRESS_PREFIX = "shippingAddress.get.";
    private static final String SHIPPING_ADDRESS_UPDATE_PREFIX = "shippingAddress.update.";
    
    private final NatsUtils natsUtils;
    private final CustomerService customerService;
    private final ShippingAddressService shippingAddressService;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    @Autowired
    public CustomerNatsListener(NatsUtils natsUtils, CustomerService customerService,
                              ShippingAddressService shippingAddressService,
                              ObjectMapper objectMapper, JwtUtil jwtUtil) {
        this.natsUtils = natsUtils;
        this.customerService = customerService;
        this.shippingAddressService = shippingAddressService;
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
        initListeners();
    }

    private void initListeners() {
        Dispatcher dispatcher = natsUtils.getConnection().createDispatcher(this::handleMessage);
        dispatcher.subscribe("customer.get");
        dispatcher.subscribe(SHIPPING_ADDRESS_PREFIX + "*");
        dispatcher.subscribe(SHIPPING_ADDRESS_UPDATE_PREFIX + "*");
    }

    private void handleMessage(Message msg) {
        String token = extractTokenFromHeaders(msg);
        if (token == null || !jwtUtil.validateToken(token)) {
            sendErrorResponse(msg.getReplyTo(), "Invalid or missing token", HttpStatus.FORBIDDEN);
            return;
        }

        authenticateUser(token);
        
        if (msg.getSubject().equals("customer.get")) {
            processCustomerRequest(msg.getReplyTo());
        } else if (msg.getSubject().startsWith(SHIPPING_ADDRESS_PREFIX)) {
            processShippingAddressRequest(msg);
        } else if (msg.getSubject().startsWith(SHIPPING_ADDRESS_UPDATE_PREFIX)) {
            processShippingAddressUpdate(msg);
        }
    }

    private void processShippingAddressUpdate(Message msg) {
        try {
            Integer addressId = extractIdFromSubject(msg.getSubject(), SHIPPING_ADDRESS_UPDATE_PREFIX);
            ShippingAddressDto addressDto = objectMapper.readValue(msg.getData(), ShippingAddressDto.class);
            ShippingAddressDto updatedAddress = shippingAddressService.updateAddress(addressDto, addressId);
            sendSuccessResponse(msg.getReplyTo(), "shippingAddress", updatedAddress);
        } catch (Exception e) {
            sendErrorResponse(msg.getReplyTo(), "Error updating shipping address", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void processCustomerRequest(String replyTo) {
        try {
            CustomerDto customer = customerService.getCurrentCustomer();
            sendSuccessResponse(replyTo, "customer", customer);
        } catch (Exception e) {
            sendErrorResponse(replyTo, "Error processing customer request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void processShippingAddressRequest(Message msg) {
        try {
            Integer addressId = extractIdFromSubject(msg.getSubject(), SHIPPING_ADDRESS_PREFIX);
            ShippingAddressDto address = shippingAddressService.getAddressById(addressId);
            sendSuccessResponse(msg.getReplyTo(), "shippingAddress", address);
        } catch (Exception e) {
            sendErrorResponse(msg.getReplyTo(), "Error retrieving shipping address", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Integer extractIdFromSubject(String subject, String prefix) {
        return Integer.parseInt(subject.replace(prefix, ""));
    }

    private void sendSuccessResponse(String replyTo, String fieldName, Object data) {
        try {
            ObjectNode responseNode = objectMapper.createObjectNode();
            responseNode.putPOJO(fieldName, data);
            natsUtils.getConnection().publish(replyTo, objectMapper.writeValueAsBytes(responseNode));
        } catch (Exception e) {
            sendErrorResponse(replyTo, "Error formatting response", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractTokenFromHeaders(Message msg) {
        Headers headers = msg.getHeaders();
        if (headers == null) return null;
        
        String authHeader = headers.getFirst("Authorization");
        return authHeader != null ? authHeader.replace("Bearer ", "").trim() : null;
    }

    private void authenticateUser(String token) {
        String userId = jwtUtil.extractUsername(token).orElseThrow();
        List<GrantedAuthority> authorities = jwtUtil.extractRoles(token)
                .orElse(List.of())
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(userId, token, authorities));
    }

    private void sendErrorResponse(String replyTo, String message, HttpStatus status) {
        if (replyTo == null) return;
        
        try {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("status", status.value());
            errorNode.put("message", message);
            natsUtils.getConnection().publish(replyTo, objectMapper.writeValueAsBytes(errorNode));
        } catch (Exception ignored) {}
    }
}