package com.radovan.spring.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.radovan.spring.entity.ShippingAddressEntity;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddressEntity, Integer> {

}
