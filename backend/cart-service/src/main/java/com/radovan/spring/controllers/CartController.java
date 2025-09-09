package com.radovan.spring.controllers;

import java.util.List;

import com.radovan.spring.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.radovan.spring.broker.CartNatsSender;
import com.radovan.spring.dto.CartDto;
import com.radovan.spring.dto.CartItemDto;
import com.radovan.spring.services.CartItemService;
import com.radovan.spring.services.CartService;

@RestController
@RequestMapping(value = "/api/cart")
public class CartController {

	private CartService cartService;
	private CartItemService cartItemService;
	private CartNatsSender cartNatsSender;

	@Autowired
	private void initialize(CartService cartService, CartItemService cartItemService, CartNatsSender cartNatsSender) {
		this.cartService = cartService;
		this.cartItemService = cartItemService;
		this.cartNatsSender = cartNatsSender;
	}

	@GetMapping(value = "/getMyItems")
	public ResponseEntity<List<CartItemDto>> listMyItems() {
		JsonNode customerData = cartNatsSender.retrieveCurrentCustomer(TokenUtils.provideToken());
		Integer cartId = customerData.get("cartId").asInt();

		List<CartItemDto> allItems = cartItemService.listAllByCartId(cartId);
		return new ResponseEntity<>(allItems, HttpStatus.OK);
	}

	@PostMapping(value = "/addCartItem/{productId}")
	public ResponseEntity<String> addItem(@PathVariable("productId") Integer productId) {
		cartItemService.addCartItem(productId,TokenUtils.provideToken());
		return new ResponseEntity<>("The item has been added to the cart!", HttpStatus.OK);
	}

	@DeleteMapping(value = "/deleteItem/{itemId}")
	public ResponseEntity<String> deleteItem(@PathVariable("itemId") Integer itemId) {
		cartItemService.removeCartItem(itemId, TokenUtils.provideToken());
		return new ResponseEntity<>("The item has been removed from the cart!", HttpStatus.OK);
	}

	@DeleteMapping(value = "/clearCart")
	public ResponseEntity<String> clearCart() {
		cartService.clearCart();
		return new ResponseEntity<>("Your cart is clear", HttpStatus.OK);
	}

	@GetMapping(value = "/getMyCart")
	public ResponseEntity<CartDto> getMyCart() {
		JsonNode customerData = cartNatsSender.retrieveCurrentCustomer(TokenUtils.provideToken());
		Integer cartId = customerData.get("cartId").asInt();

		CartDto cart = cartService.getCartById(cartId);
		return new ResponseEntity<>(cart, HttpStatus.OK);
	}

	@GetMapping(value = "/validateCart")
	public ResponseEntity<CartDto> validateCart() {
		JsonNode customerData = cartNatsSender.retrieveCurrentCustomer(TokenUtils.provideToken());
		Integer cartId = customerData.get("cartId").asInt();
		return new ResponseEntity<>(cartService.validateCart(cartId), HttpStatus.OK);
	}

}
