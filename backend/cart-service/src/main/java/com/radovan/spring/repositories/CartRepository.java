package com.radovan.spring.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.radovan.spring.entity.CartEntity;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Integer> {

	@Query(value = "select sum(price) from cart_items where cart_id = :cartId", nativeQuery = true)
	Optional<Float> calculateCartPrice(@Param("cartId") Integer cartId);
}
