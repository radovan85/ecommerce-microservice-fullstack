package com.radovan.play.repositories;

import com.radovan.play.entity.OrderItemEntity;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {

    Optional<OrderItemEntity> findById(Integer itemId);

    OrderItemEntity save(OrderItemEntity orderItemEntity);

    List<OrderItemEntity> findAll();

    List<OrderItemEntity> findAllByOrderId(Integer orderId);
}
