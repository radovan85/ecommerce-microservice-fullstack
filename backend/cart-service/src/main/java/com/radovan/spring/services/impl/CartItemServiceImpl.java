package com.radovan.spring.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.radovan.spring.broker.CartNatsSender;
import com.radovan.spring.converter.TempConverter;
import com.radovan.spring.dto.CartDto;
import com.radovan.spring.dto.CartItemDto;
import com.radovan.spring.entity.CartItemEntity;
import com.radovan.spring.exceptions.InstanceUndefinedException;
import com.radovan.spring.exceptions.OperationNotAllowedException;
import com.radovan.spring.exceptions.OutOfStockException;
import com.radovan.spring.repositories.CartItemRepository;
import com.radovan.spring.services.CartItemService;
import com.radovan.spring.services.CartService;

@Service
public class CartItemServiceImpl implements CartItemService {

	private CartItemRepository itemRepository;
	private CartService cartService;
	private TempConverter tempConverter;
	private CartNatsSender cartNatsSender;

	@Autowired
	private void initialize(CartItemRepository itemRepository, CartService cartService, TempConverter tempConverter,
			CartNatsSender cartNatsSender) {
		this.itemRepository = itemRepository;
		this.cartService = cartService;
		this.tempConverter = tempConverter;
		this.cartNatsSender = cartNatsSender;
	}

	@Override
	public CartItemDto addCartItem(Integer productId,String jwtToken) {

		// Dohvatanje korisničkih podataka
		JsonNode customerData = cartNatsSender.retrieveCurrentCustomer(jwtToken);

		// Dohvatanje podataka o proizvodu
		JsonNode productData = cartNatsSender.retrieveProductFromBroker(productId,jwtToken);

		// Validacija ID-a korpe
		Integer cartId = customerData.get("cartId").asInt();

		CartDto cart = cartService.getCartById(cartId);

		// Ekstrakcija podataka o proizvodu
		JsonNode productNode = productData.get("product");
		if (productNode == null) {
			throw new RuntimeException("ERROR: Product data is missing!");
		}

		int unitStock = productNode.get("unitStock").asInt();
		String productName = productNode.get("productName").asText();
		float productPrice = productNode.get("productPrice").floatValue();
		float discount = productNode.get("discount").floatValue();

		// Postojeći proizvod u korpi?
		Optional<CartItemDto> existingItem = listAllByCartId(cartId).stream()
				.filter(item -> item.getProductId().equals(productId)).findFirst();

		CartItemDto cartItem = existingItem.orElseGet(() -> {
			CartItemDto newItem = new CartItemDto();
			newItem.setProductId(productId);
			newItem.setCartId(cartId);
			newItem.setQuantity(1);
			return newItem;
		});

		if (existingItem.isPresent()) {
			cartItem.setQuantity(cartItem.getQuantity() + 1);
		}

		// Provera zaliha
		if (unitStock < cartItem.getQuantity()) {
			throw new OutOfStockException(new Error("There is a shortage of " + productName + " in stock!"));
		}

		// Računanje finalne cene
		float finalPrice = productPrice - ((productPrice * discount) / 100);
		finalPrice *= cartItem.getQuantity();

		// Kreiranje i čuvanje stavke korpe
		cartItem.setPrice(finalPrice);
		CartItemEntity cartItemEntity = tempConverter.cartItemDtoToEntity(cartItem);
		cartItemEntity.setCart(tempConverter.cartDtoToEntity(cart));
		CartItemEntity storedItem = itemRepository.save(cartItemEntity);

		// Osvežavanje stanja korpe
		cartService.refreshCartState(cartId);

		return tempConverter.cartItemEntityToDto(storedItem);
	}

	@Override
	public void removeCartItem(Integer itemId,String jwtToken) {
		JsonNode customerData = cartNatsSender.retrieveCurrentCustomer(jwtToken);
		Integer cartId = customerData.get("cartId").asInt();
		CartItemDto cartItem = getItemById(itemId);

		if (!cartId.equals(cartItem.getCartId())) {
			throw new OperationNotAllowedException(new Error("Operation not allowed!"));
		}

		itemRepository.deleteById(itemId);
		cartService.refreshCartState(cartId);
	}

	@Override
	public void removeAllByCartId(Integer cartId) {
		itemRepository.deleteAllByCartId(cartId);
		cartService.refreshCartState(cartId);
	}

	@Override
	public void removeAllByProductId(Integer productId) {
		itemRepository.deleteAllByProductId(productId);
		cartService.refreshAllCarts();
	}

	@Override
	public List<CartItemDto> listAllByCartId(Integer cartId) {
		return itemRepository.findAllByCartId(cartId).stream().map(tempConverter::cartItemEntityToDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<CartItemDto> listAllByProductId(Integer productId) {
		return itemRepository.findAllByProductId(productId).stream().map(tempConverter::cartItemEntityToDto)
				.collect(Collectors.toList());
	}

	@Override
	public CartItemDto getItemById(Integer itemId) {
		CartItemEntity itemEntity = itemRepository.findById(itemId)
				.orElseThrow(() -> new InstanceUndefinedException(new Error("Cart item has not been found!")));
		return tempConverter.cartItemEntityToDto(itemEntity);
	}

	@Override
	public void updateAllByProductId(Integer productId,String jwtToken) {
		JsonNode productData = cartNatsSender.retrieveProductFromBroker(productId,jwtToken);
		JsonNode productDetails = productData.get("product");

		float productPrice = productDetails.get("productPrice").floatValue();
		float discount = productDetails.get("discount").floatValue();

		List<CartItemEntity> allItems = listAllByProductId(productId).stream().map(tempConverter::cartItemDtoToEntity)
				.collect(Collectors.toList());

		allItems.forEach(cartItem -> {
			float finalPrice = productPrice - ((productPrice * discount) / 100);
			finalPrice = finalPrice * cartItem.getQuantity(); // ✅ Korekcija cene

			cartItem.setPrice(finalPrice);
			itemRepository.save(cartItem); // ✅ Ažuriranje u bazi
		});

		cartService.refreshAllCarts();
	}

}
