package com.radovan.spring.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.radovan.spring.entity.CartItemEntity;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Integer> {

	@Modifying
	@Query(value = "delete from cart_items where cart_id = :cartId", nativeQuery = true)
	void deleteAllByCartId(@Param("cartId") Integer cartId);

	@Query(value = "select * from cart_items where cart_id = :cartId", nativeQuery = true)
	List<CartItemEntity> findAllByCartId(@Param("cartId") Integer cartId);

	List<CartItemEntity> findAllByProductId(Integer productId);

	void deleteAllByProductId(Integer productId);
}