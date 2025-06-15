package com.radovan.spring.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.spring.dto.CartDto;
import com.radovan.spring.dto.CartItemDto;
import com.radovan.spring.exceptions.InvalidCartException;
import com.radovan.spring.services.CartItemService;
import com.radovan.spring.services.CartService;
import com.radovan.spring.utils.NatsUtils;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CartNatsListener {

	private static final String CART_RESPONSE_QUEUE = "cart.response";
	private static final String CART_ITEMS_PREFIX = "cart.items.list.";
	private static final String CART_REFRESH_PREFIX = "cart.refresh.";
	private static final String CART_REMOVE_ITEMS_PREFIX = "cart.items.removeAll.";

	private final NatsUtils natsUtils;
	private final CartItemService cartItemService;
	private final CartService cartService;
	private final ObjectMapper objectMapper;

	@Autowired
	public CartNatsListener(NatsUtils natsUtils, CartItemService cartItemService, CartService cartService,
			ObjectMapper objectMapper) {
		this.natsUtils = natsUtils;
		this.cartItemService = cartItemService;
		this.cartService = cartService;
		this.objectMapper = objectMapper;
		initializeListeners();
	}

	private void initializeListeners() {
		Dispatcher dispatcher = natsUtils.getConnection().createDispatcher(this::handleMessage);
		dispatcher.subscribe("cart.updateAllByProductId.*");
		dispatcher.subscribe("cart.removeAllByProductId.*");
		dispatcher.subscribe("cart.create");
		dispatcher.subscribe("cart.delete.*");
		dispatcher.subscribe("cart.getById.*");
		dispatcher.subscribe("cart.validate.*");
		dispatcher.subscribe(CART_ITEMS_PREFIX + "*");
		dispatcher.subscribe(CART_REFRESH_PREFIX + "*");
		dispatcher.subscribe(CART_REMOVE_ITEMS_PREFIX + "*");
	}

	private void handleMessage(Message msg) {
		try {
			switch (msg.getSubject()) {
			case "cart.create":
				handleCartCreate(msg);
				break;
			default:
				if (msg.getSubject().startsWith("cart.updateAllByProductId.")) {
					handleCartUpdate(msg);
				} else if (msg.getSubject().startsWith("cart.removeAllByProductId.")) {
					handleCartRemove(msg);
				} else if (msg.getSubject().startsWith("cart.delete.")) {
					handleCartDelete(msg);
				} else if (msg.getSubject().startsWith("cart.getById.")) {
					handleGetCartById(msg);
				} else if (msg.getSubject().startsWith("cart.validate.")) {
					handleValidateCart(msg);
				} else if (msg.getSubject().startsWith(CART_ITEMS_PREFIX)) {
					handleListCartItems(msg);
				} else if (msg.getSubject().startsWith(CART_REFRESH_PREFIX)) {
					handleRefreshCart(msg);
				} else if (msg.getSubject().startsWith(CART_REMOVE_ITEMS_PREFIX)) {
					handleRemoveAllItems(msg);
				}
				break;
			}
		} catch (Exception e) {
			sendErrorResponse(getReplyTo(msg), "Error processing request: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleValidateCart(Message msg) {
		try {
			Integer cartId = extractIdFromSubject(msg.getSubject(), "cart.validate.");
			CartDto cart = cartService.validateCart(cartId);
			ObjectNode responseNode = objectMapper.createObjectNode();
			responseNode.putPOJO("cart", cart);
			sendResponse(getReplyTo(msg), responseNode);
		} catch (InvalidCartException e) {
			sendErrorResponse(getReplyTo(msg), "Cart is empty and cannot be validated", HttpStatus.NOT_ACCEPTABLE);
		} catch (Exception e) {
			sendErrorResponse(getReplyTo(msg), "Failed to validate cart: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleRefreshCart(Message msg) {
		try {
			Integer cartId = extractIdFromSubject(msg.getSubject(), CART_REFRESH_PREFIX);
			cartService.refreshCartState(cartId);
			sendSuccessResponse(msg.getReplyTo(), "Cart refreshed successfully");
		} catch (Exception e) {
			sendErrorResponse(getReplyTo(msg), "Failed to refresh cart: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleRemoveAllItems(Message msg) {
		try {
			Integer cartId = extractIdFromSubject(msg.getSubject(), CART_REMOVE_ITEMS_PREFIX);
			cartItemService.removeAllByCartId(cartId);
			sendSuccessResponse(msg.getReplyTo(), "All cart items removed successfully");
		} catch (Exception e) {
			sendErrorResponse(getReplyTo(msg), "Failed to remove cart items: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleListCartItems(Message msg) {
		try {
			Integer cartId = extractIdFromSubject(msg.getSubject(), CART_ITEMS_PREFIX);
			List<CartItemDto> cartItems = cartItemService.listAllByCartId(cartId);

			ObjectNode responseNode = objectMapper.createObjectNode();
			ArrayNode itemsArray = responseNode.putArray("cartItems");

			for (CartItemDto item : cartItems) {
				itemsArray.add(objectMapper.valueToTree(item));
			}

			sendResponse(getReplyTo(msg), responseNode);
		} catch (Exception e) {
			sendErrorResponse(getReplyTo(msg), "Failed to list cart items: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleCartUpdate(Message msg) {

		String jwtToken = null;

		try {
			ObjectNode payload = objectMapper.readTree(msg.getData()).deepCopy();
			jwtToken = payload.has("Authorization") ? payload.get("Authorization").asText() : null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ✅ Postavljamo autentifikaciju u Spring Security samo ako imamo validan token
		if (jwtToken != null) {
			SecurityContextHolder.getContext().setAuthentication(
					new UsernamePasswordAuthenticationToken(null, jwtToken, Collections.emptyList()));
		}

		try {
			cartItemService.updateAllByProductId(extractProductId(msg));
		} catch (Exception e) {
			sendErrorResponse(getReplyTo(msg), "Failed to update cart items: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleCartRemove(Message msg) {
		try {
			cartItemService.removeAllByProductId(extractProductId(msg));
		} catch (Exception e) {
			sendErrorResponse(getReplyTo(msg), "Failed to remove cart items: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleCartCreate(Message msg) {
		try {
			CartDto newCart = cartService.addCart();
			ObjectNode responseNode = objectMapper.createObjectNode();
			responseNode.put("cartId", newCart.getCartId());
			sendResponse(getReplyTo(msg), responseNode);
		} catch (Exception e) {
			sendErrorResponse(getReplyTo(msg), "Failed to create cart: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleCartDelete(Message msg) {
		try {
			cartService.deleteCart(extractCartId(msg));
		} catch (Exception e) {
			sendErrorResponse(getReplyTo(msg), "Failed to delete cart: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleGetCartById(Message msg) {
		try {
			Integer cartId = extractIdFromSubject(msg.getSubject(), "cart.getById.");
			CartDto cart = cartService.getCartById(cartId);
			ObjectNode responseNode = objectMapper.createObjectNode();
			responseNode.putPOJO("cart", cart);
			sendResponse(getReplyTo(msg), responseNode);
		} catch (Exception e) {
			sendErrorResponse(getReplyTo(msg), "Failed to get cart: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private Integer extractProductId(Message msg) {
		return extractIdFromSubject(msg.getSubject(), "cart.(updateAllByProductId|removeAllByProductId)\\.");
	}

	private Integer extractCartId(Message msg) {
		return extractIdFromSubject(msg.getSubject(), "cart.delete.");
	}

	private Integer extractIdFromSubject(String subject, String prefix) {
		return Integer.parseInt(subject.replaceAll(prefix, ""));
	}

	private void sendResponse(String replyTo, ObjectNode responseNode) {
		try {
			if (replyTo != null && !replyTo.isEmpty()) {
				natsUtils.getConnection().publish(replyTo, objectMapper.writeValueAsBytes(responseNode));
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to send response", e);
		}
	}

	private void sendSuccessResponse(String replyTo, String message) {
		ObjectNode responseNode = objectMapper.createObjectNode();
		responseNode.put("status", "SUCCESS");
		responseNode.put("message", message);
		sendResponse(replyTo, responseNode);
	}

	private void sendErrorResponse(String replyTo, String errorMessage, HttpStatus status) {
		try {
			if (replyTo != null && !replyTo.isEmpty()) {
				ObjectNode errorNode = objectMapper.createObjectNode();
				errorNode.put("error", errorMessage);
				errorNode.put("status", status.value());
				errorNode.put("message", errorMessage);
				natsUtils.getConnection().publish(replyTo, objectMapper.writeValueAsBytes(errorNode));
			}
		} catch (Exception ignored) {
		}
	}

	private String getReplyTo(Message msg) {
		return msg.getReplyTo() != null && !msg.getReplyTo().isEmpty() ? msg.getReplyTo() : CART_RESPONSE_QUEUE;
	}
}