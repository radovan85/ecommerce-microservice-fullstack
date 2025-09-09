package com.radovan.play.repositories;

import com.radovan.play.entity.OrderEntity;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<OrderEntity> findById(Integer orderId);

    OrderEntity save(OrderEntity orderEntity);

    List<OrderEntity> findAll();

    List<OrderEntity> findAllByCartId(Integer cartId);

    void deleteById(Integer orderId);
}
