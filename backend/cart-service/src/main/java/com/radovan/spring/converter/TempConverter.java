package com.radovan.spring.converter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.radovan.spring.dto.CartDto;
import com.radovan.spring.dto.CartItemDto;
import com.radovan.spring.entity.CartEntity;
import com.radovan.spring.entity.CartItemEntity;
import com.radovan.spring.repositories.CartItemRepository;
import com.radovan.spring.repositories.CartRepository;

@Component
public class TempConverter {

	private ModelMapper mapper;
	private CartRepository cartRepository;
	private CartItemRepository cartItemRepository;
	private final DecimalFormat decfor = new DecimalFormat("0.00");

	@Autowired
	private void initialize(ModelMapper mapper, CartRepository cartRepository, CartItemRepository cartItemRepository) {
		this.mapper = mapper;
		this.cartRepository = cartRepository;
		this.cartItemRepository = cartItemRepository;
	}

	public CartDto cartEntityToDto(CartEntity cart) {
		CartDto returnValue = mapper.map(cart, CartDto.class);

		Optional<List<CartItemEntity>> cartItemsOptional = Optional.ofNullable(cart.getCartItems());
		cartItemsOptional.ifPresent(cartItems -> {
			List<Integer> cartItemsIds = cartItems.stream().map(CartItemEntity::getCartItemId)
					.collect(Collectors.toList());
			returnValue.setCartItemsIds(cartItemsIds);
		});

		returnValue.setCartPrice(Float.valueOf(decfor.format(returnValue.getCartPrice())));

		return returnValue;
	}

	public CartEntity cartDtoToEntity(CartDto cart) {
		CartEntity returnValue = mapper.map(cart, CartEntity.class);

		Optional<List<Integer>> cartItemsIdsOptional = Optional.ofNullable(cart.getCartItemsIds());
		List<CartItemEntity> cartItems = new ArrayList<CartItemEntity>();

		cartItemsIdsOptional.ifPresent(cartItemsIds -> {
			cartItemsIds.forEach(itemId -> {
				cartItemRepository.findById(itemId).ifPresent(cartItem -> {
					cartItems.add(cartItem);
				});
			});
		});

		returnValue.setCartItems(cartItems);
		returnValue.setCartPrice(Float.valueOf(decfor.format(returnValue.getCartPrice())));
		return returnValue;
	}

	public CartItemDto cartItemEntityToDto(CartItemEntity cartItem) {
		// TODO Auto-generated method stub
		CartItemDto returnValue = mapper.map(cartItem, CartItemDto.class);
		Optional<CartEntity> cartOptional = Optional.ofNullable(cartItem.getCart());
		if (cartOptional.isPresent()) {
			returnValue.setCartId(cartOptional.get().getCartId());
		}

		returnValue.setPrice(Float.valueOf(decfor.format(returnValue.getPrice())));
		return returnValue;
	}

	public CartItemEntity cartItemDtoToEntity(CartItemDto cartItem) {
		// TODO Auto-generated method stub
		CartItemEntity returnValue = mapper.map(cartItem, CartItemEntity.class);

		Optional<Integer> cartIdOptional = Optional.ofNullable(cartItem.getCartId());
		if (cartIdOptional.isPresent()) {
			Integer cartId = cartIdOptional.get();
			CartEntity cartEntity = cartRepository.findById(cartId).orElse(null);
			if (cartEntity != null) {
				returnValue.setCart(cartEntity);
			}
		}

		returnValue.setPrice(Float.valueOf(decfor.format(returnValue.getPrice())));
		return returnValue;
	}

}
