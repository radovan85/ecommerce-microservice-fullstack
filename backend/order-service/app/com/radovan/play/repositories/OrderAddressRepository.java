package com.radovan.play.repositories;

import com.radovan.play.entity.OrderAddressEntity;

import java.util.List;
import java.util.Optional;

public interface OrderAddressRepository {
    Optional<OrderAddressEntity> findById(Integer addressId);

    OrderAddressEntity save(OrderAddressEntity orderAddressEntity);

    List<OrderAddressEntity> findAll();
}
