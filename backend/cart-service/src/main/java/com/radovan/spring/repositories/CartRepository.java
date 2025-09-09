package com.radovan.spring.repositories;

import com.radovan.spring.entity.CartEntity;

import java.util.List;
import java.util.Optional;

public interface CartRepository {

	Optional<Float> calculateCartPrice(Integer cartId);

	Optional<CartEntity> findById(Integer cartId);

	CartEntity save(CartEntity cartEntity);

	List<CartEntity> findAll();

	void deleteById(Integer cartId);
}
