package com.radovan.spring.broker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.spring.utils.NatsUtils;
import io.nats.client.Connection;
import io.nats.client.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

	public JsonNode retrieveProductFromBroker(int productId, String jwtToken) {
		ObjectNode payload = objectMapper.createObjectNode();
		payload.put("token", jwtToken);
		payload.put("productId", productId);

		String response = sendRequest("product.get." + productId, payload.toString());
		JsonNode json;
		try {
			json = objectMapper.readTree(response);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse response", e);
		}

		if (json.has("status") && json.get("status").asInt() == 422) {
			String msg = json.has("message") ? json.get("message").asText() : "Product not found";
			throw new RuntimeException(msg);
		}

		return json;
	}

	public JsonNode retrieveCurrentCustomer(String jwtToken) {
		ObjectNode payload = objectMapper.createObjectNode();
		payload.put("token", jwtToken);

		String response = sendRequest("customer.getCurrent", payload.toString());
		JsonNode json;
		try {
			json = objectMapper.readTree(response);
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse response", e);
		}

		if (json.has("status") && json.get("status").asInt() == 500) {
			String msg = json.has("error") ? json.get("error").asText() : "Failed to retrieve current customer";
			throw new RuntimeException(msg);
		}

		return json;
	}

	private String sendRequest(String subject, String payload) {
		Connection connection = natsUtils.getConnection();
		if (connection == null) {
			throw new RuntimeException("NATS connection is not initialized");
		}

		try {
			CompletableFuture<Message> future = connection.request(
					subject,
					payload.getBytes(StandardCharsets.UTF_8)
			);

			Message msg = future.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			return new String(msg.getData(), StandardCharsets.UTF_8);
		} catch (TimeoutException e) {
			throw new RuntimeException("NATS request timeout for subject: " + subject, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("NATS request interrupted for subject: " + subject, e);
		} catch (Exception e) {
			throw new RuntimeException("NATS request failed for subject: " + subject, e);
		}
	}
}