package com.radovan.spring.brokers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.spring.converter.DeserializeConverter;
import com.radovan.spring.dto.UserDto;
import com.radovan.spring.exceptions.ExistingInstanceException;
import com.radovan.spring.services.UserService;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMessageListener {

	private NatsUtils natsUtils;
	private UserService userService;
	private ObjectMapper objectMapper;
	private JwtUtil jwtUtil;
	private DeserializeConverter deserializeConverter;

	@Autowired
	private void initialize(NatsUtils natsUtils, UserService userService, ObjectMapper objectMapper, JwtUtil jwtUtil,
			DeserializeConverter deserializeConverter) {
		this.natsUtils = natsUtils;
		this.userService = userService;
		this.objectMapper = objectMapper;
		this.jwtUtil = jwtUtil;
		this.deserializeConverter = deserializeConverter;
		initListeners();
	}

	private void initListeners() {
		Dispatcher dispatcher = natsUtils.getConnection().createDispatcher(this::handleMessage);
		dispatcher.subscribe("user.get");
		dispatcher.subscribe("user.delete.*");
		dispatcher.subscribe("user.create");
		dispatcher.subscribe("user.suspend.*");
		dispatcher.subscribe("user.reactivate.*");
	}

	private void handleMessage(Message msg) {
		try {
			switch (msg.getSubject()) {
			case "user.get":
				handleUserGet(msg);
				break;
			case "user.create":
				handleUserCreate(msg);
				break;
			default:
				if (msg.getSubject().startsWith("user.delete.")) {
					handleUserDelete(msg);
				} else if (msg.getSubject().startsWith("user.suspend.")) {
					handleUserSuspend(msg);
				} else if (msg.getSubject().startsWith("user.reactivate.")) {
					handleUserReactivate(msg);
				}
				break;
			}
		} catch (Exception e) {
			sendErrorResponse(getReplyTo(msg), "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleUserGet(Message msg) throws Exception {
		JsonNode request = objectMapper.readTree(msg.getData());
		String token = request.get("token").asText();
		authenticateUser(token);

		UserDto currentUser = userService.getCurrentUser();
		if (currentUser.getEnabled() == 0) {
			sendErrorResponse(msg.getReplyTo(), "Account suspended", HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
			return;
		}

		natsUtils.getConnection().publish(msg.getReplyTo(), objectMapper.writeValueAsBytes(currentUser));
	}

	private void handleUserCreate(Message msg) {
		try {
			UserDto userDto = deserializeConverter.payloadToUserDto(new String(msg.getData(), StandardCharsets.UTF_8));
			UserDto createdUser = userService.addUser(userDto);

			ObjectNode response = objectMapper.createObjectNode();
			response.put("id", createdUser.getId());
			response.put("status", HttpStatus.OK.value());

			natsUtils.getConnection().publish("user.response", objectMapper.writeValueAsBytes(response));
		} catch (ExistingInstanceException e) {
			sendErrorResponse("user.response", "Email already exists", HttpStatus.CONFLICT);
		} catch (Exception e) {
			sendErrorResponse("user.response", "Error creating user", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleUserDelete(Message msg) {
		processUserOperation(msg, userId -> userService.deleteUser(userId), "User ID %d successfully deleted");
	}

	private void handleUserSuspend(Message msg) {
		processUserOperation(msg, userId -> userService.suspendUser(userId), "User ID %d suspended");
	}

	private void handleUserReactivate(Message msg) {
		processUserOperation(msg, userId -> userService.reactivateUser(userId), "User ID %d reactivated");
	}

	private void processUserOperation(Message msg, UserOperation operation, String successMessage) {
		try {
			Integer userId = extractUserId(msg);
			String replyTo = getReplyTo(msg);

			operation.execute(userId);

			ObjectNode response = objectMapper.createObjectNode();
			response.put("status", HttpStatus.OK.value());
			response.put("message", String.format(successMessage, userId));

			natsUtils.getConnection().publish(replyTo, objectMapper.writeValueAsBytes(response));
		} catch (Exception e) {
			sendErrorResponse("user.response", "Error processing operation", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void authenticateUser(String token) {
		String userId = jwtUtil.extractUsername(token);
		List<GrantedAuthority> authorities = jwtUtil.extractRoles(token).stream().map(SimpleGrantedAuthority::new)
				.collect(Collectors.toUnmodifiableList());

		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(userId, token, authorities));
	}

	private void sendErrorResponse(String replyTo, String message, HttpStatus status) {
		if (replyTo == null || replyTo.isEmpty())
			return;

		try {
			ObjectNode errorNode = objectMapper.createObjectNode();
			errorNode.put("status", status.value());
			errorNode.put("message", message);
			natsUtils.getConnection().publish(replyTo, objectMapper.writeValueAsBytes(errorNode));
		} catch (Exception ignored) {
		}
	}

	private Integer extractUserId(Message msg) {
		return Integer.parseInt(msg.getSubject().replaceAll("user.(delete|suspend|reactivate)\\.", ""));
	}

	private String getReplyTo(Message msg) {
		Headers headers = msg.getHeaders();
		String replyTo = msg.getReplyTo();
		return (replyTo == null || replyTo.isEmpty())
				? (headers != null ? headers.getFirst("Nats-Reply-To") : "user.response")
				: replyTo;
	}

	@FunctionalInterface
	private interface UserOperation {
		void execute(Integer userId) throws Exception;
	}
}