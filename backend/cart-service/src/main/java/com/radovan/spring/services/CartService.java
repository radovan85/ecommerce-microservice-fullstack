package com.radovan.spring.services;

import com.radovan.spring.dto.CartDto;

public interface CartService {

	CartDto getCartById(Integer cartId);

	CartDto validateCart(Integer cartId);

	void refreshCartState(Integer cartId);

	void refreshAllCarts();

	CartDto addCart();

	void deleteCart(Integer cartId);
	
	void clearCart();
}