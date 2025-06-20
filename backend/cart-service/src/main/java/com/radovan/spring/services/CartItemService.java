package com.radovan.spring.services;

import java.util.List;

import com.radovan.spring.dto.CartItemDto;

public interface CartItemService {

	void removeAllByCartId(Integer cartId);

	void removeAllByProductId(Integer productId);

	List<CartItemDto> listAllByCartId(Integer cartId);

	CartItemDto getItemById(Integer itemId);

	CartItemDto addCartItem(Integer productId);

	void removeCartItem(Integer itemId);

	List<CartItemDto> listAllByProductId(Integer productId);

	void updateAllByProductId(Integer productId);

}