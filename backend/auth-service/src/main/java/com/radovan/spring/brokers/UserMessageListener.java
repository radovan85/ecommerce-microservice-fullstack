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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class UserMessageListener {

	private NatsUtils natsUtils;
	private UserService userService;
	private ObjectMapper objectMapper;
	private JwtUtil jwtUtil;
	private DeserializeConverter deserializeConverter;

	@Autowired
	public void initialize(
			NatsUtils natsUtils,
			UserService userService,
			ObjectMapper objectMapper,
			JwtUtil jwtUtil,
			DeserializeConverter deserializeConverter
	) {
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
		dispatcher.subscribe("user.getById.*");
	}

	private void handleMessage(Message msg) {
		try {
			String subject = msg.getSubject();
			if ("user.get".equals(subject)) {
				handleUserGet(msg);
			} else if ("user.create".equals(subject)) {
				handleUserCreate(msg);
			} else if (subject.startsWith("user.getById.")) {
				handleUserGetById(msg);
			} else if (subject.startsWith("user.delete.")) {
				handleUserDelete(msg);
			} else if (subject.startsWith("user.suspend.")) {
				handleUserSuspend(msg);
			} else if (subject.startsWith("user.reactivate.")) {
				handleUserReactivate(msg);
			}
			// ignore unknown subjects
		} catch (Exception e) {
			sendErrorResponse(msg, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleUserGet(Message msg) throws Exception {
		JsonNode request = objectMapper.readTree(msg.getData());
		String token = request.get("token").asText();
		authenticateUser(token);

		UserDto currentUser = userService.getCurrentUser();
		if (currentUser.getEnabled() == 0) {
			sendErrorResponse(msg, "Account suspended", HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
			return;
		}

		natsUtils.getConnection().publish(msg.getReplyTo(), objectMapper.writeValueAsBytes(currentUser));
	}

	private void handleUserCreate(Message msg) {
		try {
			String payload = new String(msg.getData(), StandardCharsets.UTF_8);
			UserDto userDto = deserializeConverter.payloadToUserDto(payload);
			UserDto createdUser = userService.addUser(userDto);

			ObjectNode response = objectMapper.createObjectNode();
			response.put("id", createdUser.getId());
			response.put("status", HttpStatus.OK.value());

			String replyTo = msg.getReplyTo();
			if (replyTo != null && !replyTo.isEmpty()) {
				natsUtils.getConnection().publish(replyTo, objectMapper.writeValueAsBytes(response));
			}

		} catch (ExistingInstanceException e) {
			sendErrorResponse(msg, "Email already exists", HttpStatus.CONFLICT);
		} catch (Exception e) {
			sendErrorResponse(msg, "Error creating user", HttpStatus.INTERNAL_SERVER_ERROR);
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

	private void processUserOperation(Message msg, Consumer<Integer> operation, String successMessage) {
		try {
			int userId = extractUserId(msg);
			String replyTo = getReplyTo(msg);

			operation.accept(userId);

			ObjectNode response = objectMapper.createObjectNode();
			response.put("status", HttpStatus.OK.value());
			response.put("message", String.format(successMessage, userId));

			natsUtils.getConnection().publish(replyTo, objectMapper.writeValueAsBytes(response));
		} catch (Exception e) {
			sendErrorResponse(msg, "Error processing operation", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleUserGetById(Message msg) {
		try {
			String subject = msg.getSubject();
			int userId = Integer.parseInt(subject.replace("user.getById.", ""));

			UserDto user = userService.getUserById(userId);
			if (user != null) {
				if (user.getEnabled() == 0) {
					sendErrorResponse(msg, "Account suspended", HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS);
				} else {
					natsUtils.getConnection().publish(msg.getReplyTo(), objectMapper.writeValueAsBytes(user));
				}
			} else {
				sendErrorResponse(msg, "User ID " + userId + " not found", HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			sendErrorResponse(msg, "Error fetching user by ID", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void authenticateUser(String token) {
		String userId = jwtUtil.extractUsername(token);
		List<String> roles = jwtUtil.extractRoles(token);

		List<SimpleGrantedAuthority> authorities = roles.stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(userId, token, authorities);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private void sendErrorResponse(Message msg, String message, HttpStatus status) {
		String replyTo = msg.getReplyTo();
		if (replyTo == null || replyTo.isEmpty()) {
			return;
		}

		try {
			ObjectNode errorNode = objectMapper.createObjectNode();
			errorNode.put("status", status.value());
			errorNode.put("message", message);

			natsUtils.getConnection().publish(replyTo, objectMapper.writeValueAsBytes(errorNode));
		} catch (Exception e) {
			// ignore exceptions while sending error
		}
	}

	private int extractUserId(Message msg) {
		String subject = msg.getSubject();
		String userIdStr = subject.replaceAll("user.(delete|suspend|reactivate)\\.", "");
		return Integer.parseInt(userIdStr);
	}

	private String getReplyTo(Message msg) {
		Headers headers = msg.getHeaders();
		String replyTo = msg.getReplyTo();

		if (replyTo == null || replyTo.isEmpty()) {
			if (headers != null) {
				return headers.getFirst("Nats-Reply-To");
			} else {
				return "user.response";
			}
		}
		return replyTo;
	}
}