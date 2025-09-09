package com.radovan.spring.repositories;

import com.radovan.spring.entity.CartItemEntity;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository {

	void deleteAllByCartId(Integer cartId);

	void deleteAllByProductId(Integer productId);

	List<CartItemEntity> findAllByCartId(Integer cartId);

	List<CartItemEntity> findAllByProductId(Integer productId);

	Optional<CartItemEntity> findById(Integer itemId);

	void deleteById(Integer itemId);

	CartItemEntity save(CartItemEntity itemEntity);
}
