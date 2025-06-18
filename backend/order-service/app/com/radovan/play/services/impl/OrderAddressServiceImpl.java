package com.radovan.play.services.impl;

import com.radovan.play.converter.TempConverter;
import com.radovan.play.dto.OrderAddressDto;
import com.radovan.play.entity.OrderAddressEntity;
import com.radovan.play.repositories.OrderAddressRepository;
import com.radovan.play.services.OrderAddressService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class OrderAddressServiceImpl implements OrderAddressService {

    private OrderAddressRepository addressRepository;
    private TempConverter tempConverter;

    @Inject
    private void initialize(OrderAddressRepository addressRepository, TempConverter tempConverter) {
        this.addressRepository = addressRepository;
        this.tempConverter = tempConverter;
    }

    @Override
    public List<OrderAddressDto> listAll() {
        List<OrderAddressEntity> allAddresses = addressRepository.findAll();
        return allAddresses.stream().map(tempConverter::orderAddressEntityToDto).collect(Collectors.toList());
    }

}
