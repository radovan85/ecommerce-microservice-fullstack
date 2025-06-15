package com.radovan.spring.broker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radovan.spring.exceptions.InstanceUndefinedException;
import com.radovan.spring.utils.NatsUtils;
import io.nats.client.Message;
import io.nats.client.impl.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class CartNatsSender {

	private static final int REQUEST_TIMEOUT_SECONDS = 5;

	private final NatsUtils natsUtils;
	private final ObjectMapper objectMapper;

	@Autowired
	public CartNatsSender(NatsUtils natsUtils, ObjectMapper objectMapper) {
		this.natsUtils = natsUtils;
		this.objectMapper = objectMapper;
	}

	public JsonNode retrieveCurrentCustomer() {
		JsonNode customerResponse = sendRequest("customer.get", createAuthHeaders());

		if (customerResponse == null || !customerResponse.has("customer")) {
			throw new RuntimeException("Invalid customer data received from NATS");
		}

		JsonNode customer = customerResponse.get("customer");

		if (customer == null || !customer.has("cartId")) {
			throw new RuntimeException("Invalid customer object received from NATS");
		}

		return customer;
	}

	
	public JsonNode retrieveProductFromBroker(Integer productId) {
		// Dobijamo validan JWT token iz postojeće metode
		Headers headers = createAuthHeaders();

		// Dodajemo Product-ID u već kreirani headers
		headers.add("Product-ID", productId.toString());
		
		// Šaljemo zahtev na product-service
		JsonNode response = sendRequest("product.get." + productId, headers);
		// Proveravamo status odgovora
		checkProductResponse(response);
		return response;
	}
	
	
	/*
	public JsonNode retrieveProductFromBroker(Integer productId) {
		Headers headers = createAuthHeaders();
		System.out.println("Authorization: " + headers);

		// Dodajemo Product-ID u već kreirani headers
		headers.add("Product-ID", productId.toString());
		
		JsonNode response = null;
		try {
		    response = sendRequest("product.get." + productId, headers);
		    System.out.println("Received raw response: " + response);
		} catch (Exception e) {
		    System.out.println("Error in sendRequest: " + e.getMessage());
		}
		// Proveravamo status odgovora
		checkProductResponse(response);
		return response;
	}
	*/

	private void checkProductResponse(JsonNode response) {
		if (response.has("status") && response.get("status").asInt() == 422) {
			String errorMessage = response.has("message") ? response.get("message").asText() : "Product not found";
			throw new InstanceUndefinedException(new Error(errorMessage));
		}
	}

	private JsonNode sendRequest(String subject, Headers headers) {
		try {
			CompletableFuture<Message> response = natsUtils.getConnection().request(subject, headers, new byte[0]);
			Message msg = response.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			return objectMapper.readTree(msg.getData());
		} catch (Exception e) {
			throw new RuntimeException("NATS request failed for subject: " + subject, e);
		}
	}

	private Headers createAuthHeaders() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		validateAuthentication(authentication);

		String token = getTokenFromAuthentication(authentication);
		return createAuthorizationHeaders(token);
	}

	private void validateAuthentication(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new RuntimeException("User is not authenticated");
		}
	}

	private String getTokenFromAuthentication(Authentication authentication) {
		if (!(authentication.getCredentials() instanceof String)) {
			throw new RuntimeException("Missing authorization token");
		}
		return (String) authentication.getCredentials();
	}

	private Headers createAuthorizationHeaders(String token) {
		Headers headers = new Headers();
		headers.add("Authorization", "Bearer " + token);
		return headers;
	}

}
