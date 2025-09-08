package com.radovan.spring.repositories;

import org.springframework.stereotype.Repository;

import com.radovan.spring.entity.ShippingAddressEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingAddressRepository {

    Optional<ShippingAddressEntity> findById(Integer addressId);

    ShippingAddressEntity save(ShippingAddressEntity addressEntity);

    List<ShippingAddressEntity> findAll();
}
