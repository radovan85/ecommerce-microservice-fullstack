package com.radovan.play.services;

import com.radovan.play.dto.OrderAddressDto;

import java.util.List;

public interface OrderAddressService {

    List<OrderAddressDto> listAll();
}