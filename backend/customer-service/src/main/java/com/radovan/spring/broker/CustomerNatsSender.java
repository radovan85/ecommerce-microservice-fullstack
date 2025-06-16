package com.radovan.spring.broker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.spring.exceptions.SuspendedUserException;
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
public class CustomerNatsSender {

	private static final int REQUEST_TIMEOUT_SECONDS = 5;
	private static final String USER_RESPONSE_QUEUE = "user.response";

	private final NatsUtils natsUtils;
	private final ObjectMapper objectMapper;

	@Autowired
	public CustomerNatsSender(NatsUtils natsUtils, ObjectMapper objectMapper) {
		this.natsUtils = natsUtils;
		this.objectMapper = objectMapper;
	}

	public JsonNode retrieveCurrentUser() {
		try {
			byte[] payload = createTokenPayload();
			Message response = sendRequest("user.get", payload);
			JsonNode responseNode = parseResponse(response);
			validateResponse(responseNode);
			return responseNode;
		} catch (SuspendedUserException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Failed to retrieve current user", e);
		}
	}

	public void sendDeleteUserEvent(Integer userId) {
		sendUserEvent("user.delete." + userId, userId);
	}

	public void sendSuspendUserEvent(Integer userId) {
		sendUserEvent("user.suspend." + userId, userId);
	}

	public void sendReactivateUserEvent(Integer userId) {
		sendUserEvent("user.reactivate." + userId, userId);
	}

	public void sendDeleteCartEvent(Integer cartId) {
		sendEvent("cart.delete." + cartId);
	}

	public void sendDeleteOrdersByCartId(Integer cartId) {
		try {
			Headers headers = createAuthHeaders();
			String subject = "order.deleteAllByCartId." + cartId;

			CompletableFuture<Message> response = natsUtils.getConnection().request(subject, headers, new byte[0]);
			Message msg = response.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

			JsonNode responseNode = objectMapper.readTree(msg.getData());

			if (responseNode.has("error")) {
				throw new RuntimeException("Failed to delete orders: " + responseNode.get("error").asText());
			}
		} catch (Exception e) {
			throw new RuntimeException("NATS request failed for order deletion: " + e.getMessage(), e);
		}
	}

	private byte[] createTokenPayload() throws Exception {
		ObjectNode request = objectMapper.createObjectNode();
		request.put("token", getCurrentToken());
		return objectMapper.writeValueAsBytes(request);
	}

	private Message sendRequest(String subject, byte[] payload) throws Exception {
		CompletableFuture<Message> response = natsUtils.getConnection().request(subject, payload);
		return response.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
	}

	private JsonNode parseResponse(Message msg) throws Exception {
		return objectMapper.readTree(msg.getData());
	}

	private void validateResponse(JsonNode response) {
		if (response.has("status")) {
			int status = response.get("status").asInt();
			if (status == 451) {
				String message = response.path("message").asText("Account suspended");
				throw new SuspendedUserException(new Error(message));
			}
		}
	}

	private String getCurrentToken() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new RuntimeException("User is not authenticated");
		}
		return authentication.getCredentials().toString();
	}

	private void sendUserEvent(String subject, Integer userId) {
		try {
			byte[] payload = createUserEventPayload(userId);
			Headers headers = createResponseHeaders();
			natsUtils.getConnection().publish(subject, headers, payload);
		} catch (Exception e) {
			throw new RuntimeException("Error sending user event: " + subject, e);
		}
	}

	private byte[] createUserEventPayload(Integer userId) throws Exception {
		ObjectNode request = objectMapper.createObjectNode();
		request.put("userId", userId);
		return objectMapper.writeValueAsBytes(request);
	}

	private Headers createResponseHeaders() {
		Headers headers = new Headers();
		headers.add("Nats-Reply-To", USER_RESPONSE_QUEUE);
		return headers;
	}

	private void sendEvent(String subject) {
		natsUtils.getConnection().publish(subject, createAuthHeaders(), new byte[0]);
	}

	private Headers createAuthHeaders() {
		Headers headers = new Headers();
		headers.add("Authorization", "Bearer " + getCurrentToken());
		return headers;
	}
}