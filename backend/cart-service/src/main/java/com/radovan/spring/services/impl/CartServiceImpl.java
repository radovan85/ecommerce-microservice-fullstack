package com.radovan.spring.services.impl;

import java.util.List;

import com.radovan.spring.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.radovan.spring.broker.CartNatsSender;
import com.radovan.spring.converter.TempConverter;
import com.radovan.spring.dto.CartDto;
import com.radovan.spring.entity.CartEntity;
import com.radovan.spring.exceptions.InstanceUndefinedException;
import com.radovan.spring.exceptions.InvalidCartException;
import com.radovan.spring.repositories.CartRepository;
import com.radovan.spring.services.CartItemService;
import com.radovan.spring.services.CartService;

@Service
public class CartServiceImpl implements CartService {

	private CartRepository cartRepository;
	private TempConverter tempConverter;
	private CartNatsSender cartNatsSender;
	private CartItemService cartItemService;

	@Autowired
	private void initialize(CartRepository cartRepository, TempConverter tempConverter, CartNatsSender cartNatsSender,
			CartItemService cartItemService) {
		this.cartRepository = cartRepository;
		this.tempConverter = tempConverter;
		this.cartNatsSender = cartNatsSender;
		this.cartItemService = cartItemService;
	}

	@Override
	public CartDto getCartById(Integer cartId) {
		CartEntity cartEntity = cartRepository.findById(cartId)
				.orElseThrow(() -> new InstanceUndefinedException(new Error("The cart has not been found")));

		return tempConverter.cartEntityToDto(cartEntity);
	}

	@Override
	public CartDto validateCart(Integer cartId) {
		CartDto cart = getCartById(cartId);
		if (cart.getCartItemsIds().isEmpty()) {
			throw new InvalidCartException(new Error("Your cart is currently empty!"));
		}
		return cart;
	}

	@Override
	public void refreshCartState(Integer cartId) {
		CartDto cart = getCartById(cartId);
		Float cartPrice = cartRepository.calculateCartPrice(cartId).orElse(0f);
		cart.setCartPrice(cartPrice);
		cartRepository.save(tempConverter.cartDtoToEntity(cart));
	}

	@Override
	public void refreshAllCarts() {
		List<CartEntity> allCarts = cartRepository.findAll();
		allCarts.forEach(cartEntity -> refreshCartState(cartEntity.getCartId()));
	}

	@Override
	public CartDto addCart() {
		CartDto cartDto = new CartDto();
		cartDto.setCartPrice(0f);
		CartEntity storedCart = cartRepository.save(tempConverter.cartDtoToEntity(cartDto));
		return tempConverter.cartEntityToDto(storedCart);
	}

	@Override
	public void deleteCart(Integer cartId) {
		getCartById(cartId);
		cartRepository.deleteById(cartId);
	}

	@Override
	public void clearCart() {
		JsonNode customerData = cartNatsSender.retrieveCurrentCustomer(TokenUtils.provideToken());
		Integer cartId = customerData.get("cartId").asInt();
		cartItemService.removeAllByCartId(cartId);
		refreshCartState(cartId);
	}

}
